package com.ecocircular.ecocircular.iam.application;

import com.ecocircular.ecocircular.common.audit.AuditContext;
import com.ecocircular.ecocircular.common.audit.AuditEvents;
import com.ecocircular.ecocircular.common.audit.AuditService;
import com.ecocircular.ecocircular.iam.domain.Tenant;
import com.ecocircular.ecocircular.iam.domain.User;
import com.ecocircular.ecocircular.iam.domain.UserTenantRole;
import com.ecocircular.ecocircular.iam.infrastructure.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TenantRepository tenantRepository;

    private final UserRepository           userRepository;
    private final UserTenantRoleRepository userTenantRoleRepository;
    private final PasswordEncoder          passwordEncoder;
    private final JwtService               jwtService;
    private final AuditService             auditService;

    public String login(String email, String password, String clientIp) {
        User user = userRepository.findByEmail(email).orElse(null);

        // ── Login fallido: usuario no existe ──────────────────────────
        if (user == null) {
            auditService.registrar(
                    "User",
                    UUID.fromString("00000000-0000-0000-0000-000000000000"),
                    AuditEvents.SESION_FALLIDA,
                    null,
                    new LoginFallidoSnapshot(email, "Usuario no encontrado"),
                    null, null, email, clientIp, "Usuario no encontrado"
            );
            throw new RuntimeException("Credenciales inválidas");
        }

        // ── Login fallido: contraseña incorrecta ──────────────────────
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            auditService.registrar(
                    "User",
                    user.getId(),
                    AuditEvents.SESION_FALLIDA,
                    null,
                    new LoginFallidoSnapshot(email, "Contraseña incorrecta"),
                    user.getActiveTenant().getId(),
                    user.getId(), email, clientIp, "Contraseña incorrecta"
            );
            throw new RuntimeException("Credenciales inválidas");
        }

        List<UserTenantRole> roles = userTenantRoleRepository.findByUserId(user.getId());

        UserTenantRole currentRole = roles.stream()
                .filter(r -> r.getTenant().getId().equals(user.getActiveTenant().getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Usuario sin acceso a este tenant"));

        List<String> roleList       = List.of(currentRole.getRole());
        List<String> permissionList = getPermissionsForRole(currentRole.getRole());

        String token = jwtService.generateToken(
                email, user.getId(),
                user.getActiveTenant().getId(),
                roleList, permissionList);

        // ── Login exitoso ─────────────────────────────────────────────
        auditService.registrar(
                "User",
                user.getId(),
                AuditEvents.SESION_INICIADA,
                null,
                new LoginSnapshot(email, currentRole.getRole(), user.getActiveTenant().getId()),
                user.getActiveTenant().getId(),
                user.getId(), email, clientIp, null
        );

        return token;
    }

    private List<String> getPermissionsForRole(String role) {
        return switch (role) {
            case "ROLE_MUNICIPALITY_ADMIN"   -> List.of("CREATE_CAMPAIGN", "MANAGE_GREEN_POINTS",
                    "VIEW_ALL_ANALYTICS", "MANAGE_ZONES", "VIEW_AUDIT_TRAIL", "EXPORT_REPORTS");
            case "ROLE_GREEN_POINT_OPERATOR" -> List.of("VALIDATE_DELIVERY", "VIEW_LOCAL_HISTORY",
                    "REPORT_INCIDENT", "MANAGE_BATCH", "INIT_DELIVERY");
            case "ROLE_RECYCLER"             -> List.of("RECEIVE_BATCH", "REPORT_PROCESSING",
                    "VIEW_DEMAND_FORECAST", "UPDATE_MATERIAL_DEMAND");
            case "ROLE_CITIZEN"              -> List.of("INIT_DELIVERY", "VIEW_OWN_IMPACT",
                    "VIEW_RANKING", "JOIN_MISSIONS");
            default                          -> List.of();
        };
    }

    public Map<String, String> registerToTenant(String email, String password, UUID tenantId, String clientIp) {
        // 1. Autenticar al usuario (igual que en login, pero sin validar roles)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            auditService.registrar("User", user.getId(), AuditEvents.SESION_FALLIDA,
                    null, new LoginFallidoSnapshot(email, "Contraseña incorrecta"),
                    user.getActiveTenant() != null ? user.getActiveTenant().getId() : null,
                    user.getId(), email, clientIp, "Contraseña incorrecta");
            throw new RuntimeException("Credenciales inválidas");
        }

        // 2. Verificar / crear rol en el tenant solicitado
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant no encontrado"));

        Optional<UserTenantRole> existingRole = userTenantRoleRepository
                .findByUserIdAndTenantId(user.getId(), tenantId);

        if (existingRole.isEmpty()) {
            UserTenantRole newRole = new UserTenantRole();
            newRole.setUser(user);
            newRole.setTenant(tenant);
            newRole.setRole("ROLE_CITIZEN"); // Rol por defecto para ciudadanos
            userTenantRoleRepository.save(newRole);
        }

        // 3. Actualizar el activeTenant del usuario
        user.setActiveTenant(tenant);
        userRepository.save(user);

        // 4. Generar token JWT con los nuevos permisos
        List<UserTenantRole> roles = userTenantRoleRepository.findByUserId(user.getId());
        UserTenantRole currentRole = roles.stream()
                .filter(r -> r.getTenant().getId().equals(tenantId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Error al asignar el rol"));

        List<String> roleList = List.of(currentRole.getRole());
        List<String> permissionList = getPermissionsForRole(currentRole.getRole());

        String token = jwtService.generateToken(email, user.getId(), tenantId, roleList, permissionList);

        // 5. Auditoría de registro exitoso en el tenant
        auditService.registrar("User", user.getId(), AuditEvents.USUARIO_ROL_ASIGNADO, // evento nuevo, puedes crearlo
                null, Map.of("email", email, "tenant", tenantId.toString(), "role", "ROLE_CITIZEN"),
                tenantId, user.getId(), email, clientIp, null);

        return Map.of(
                "token", token,
                "id", user.getId().toString(),
                "name", user.getDisplayName()
        );
    }

    record LoginSnapshot(String email, String role, UUID tenantId) {}
    record LoginFallidoSnapshot(String email, String motivo) {}
}