// com.ecocircular.ecocircular.iam.application.UserService.java
package com.ecocircular.ecocircular.iam.application;

import com.ecocircular.ecocircular.common.audit.AuditContext;
import com.ecocircular.ecocircular.common.audit.AuditEvents;
import com.ecocircular.ecocircular.common.audit.AuditService;
import com.ecocircular.ecocircular.common.base.TenantContext;
import com.ecocircular.ecocircular.iam.domain.Tenant;
import com.ecocircular.ecocircular.iam.domain.User;
import com.ecocircular.ecocircular.iam.domain.UserTenantRole;
import com.ecocircular.ecocircular.iam.dto.AssignRoleRequest;
import com.ecocircular.ecocircular.iam.dto.UserCreateRequest;
import com.ecocircular.ecocircular.iam.dto.UserResponse;
import com.ecocircular.ecocircular.iam.dto.UserUpdateRequest;
import com.ecocircular.ecocircular.iam.infrastructure.persistence.TenantRepository;
import com.ecocircular.ecocircular.iam.infrastructure.persistence.UserRepository;
import com.ecocircular.ecocircular.iam.infrastructure.persistence.UserTenantRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private final TenantRepository tenantRepository;
    @Autowired
    private final UserTenantRoleRepository userTenantRoleRepository;
    @Autowired
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuditService auditService;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        // validar que el email no esté en uso (incluyendo eliminados lógicos)
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setDisplayName(request.getDisplayName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        // Resolver el Tenant desde el UUID del request
        if (request.getActiveTenantId() != null) {
            Tenant tenant = tenantRepository.findById(request.getActiveTenantId())
                    .orElseThrow(() -> new RuntimeException("Tenant not found"));
            user.setActiveTenant(tenant);
        }
        user.setCreatedAt(LocalDateTime.now());
        // deletedAt = null por defecto

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toResponse(user);
    }
    public UserResponse getUserByEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAllByDeletedAtIsNull(pageable)
                .map(this::toResponse);
    }

    @Transactional
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // validar email único si cambia
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        user.setEmail(request.getEmail());
        user.setDisplayName(request.getDisplayName());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getActiveTenantId() != null) {
            Tenant tenant = tenantRepository.findById(request.getActiveTenantId())
                    .orElseThrow(() -> new RuntimeException("Tenant not found"));
            user.setActiveTenant(tenant);
        }

        User updated = userRepository.save(user);
        return toResponse(updated);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setDisplayName(user.getDisplayName());
        response.setCreatedAt(user.getCreatedAt());

        if (user.getActiveTenant() != null) {
            response.setActiveTenantId(user.getActiveTenant().getId());
            response.setActiveTenantName(user.getActiveTenant().getName());
        }
        return response;
    }

    @Transactional
    public void assignRoleToUserInCurrentTenant(AssignRoleRequest request) {
        // 1. Obtener el tenant del administrador autenticado
        UUID adminTenantId = TenantContext.getTenantId();
        if (adminTenantId == null) {
            throw new IllegalStateException("No se pudo determinar el tenant del administrador");
        }

        // 2. Buscar o crear el usuario
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            // Crear nuevo usuario si vienen los datos necesarios
            if (request.getDisplayName() == null || request.getPassword() == null) {
                throw new IllegalArgumentException("Para crear un usuario nuevo se requiere displayName y password");
            }
            user = new User();
            user.setEmail(request.getEmail());
            user.setDisplayName(request.getDisplayName());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            // El activeTenant se asignará después, pero por ahora puede ser null
            user = userRepository.save(user);
        }

        // 3. Verificar si ya tiene ese rol en el tenant
        Tenant tenant = tenantRepository.findById(adminTenantId)
                .orElseThrow(() -> new IllegalStateException("Tenant no encontrado"));

        boolean alreadyHasRole = userTenantRoleRepository
                .findByUserIdAndTenantId(user.getId(), adminTenantId)
                .stream()
                .anyMatch(r -> r.getRole().equals(request.getRole()));

        if (alreadyHasRole) {
            // Podrías lanzar excepción o simplemente ignorar
            return; // o throw new IllegalArgumentException("El usuario ya tiene ese rol en este tenant");
        }

        // 4. Asignar el rol
        UserTenantRole newRole = new UserTenantRole();
        newRole.setUser(user);
        newRole.setTenant(tenant);
        newRole.setRole(request.getRole());
        userTenantRoleRepository.save(newRole);

        // 5. Si es la primera vez que el usuario tiene un rol en este tenant,
        //    establecerlo como activeTenant (opcional)
        if (user.getActiveTenant() == null || !user.getActiveTenant().getId().equals(adminTenantId)) {
            user.setActiveTenant(tenant);
            userRepository.save(user);
        }

        // 6. Auditoría
        auditService.registrar(
                "User", user.getId(),
                AuditEvents.USUARIO_ROL_ASIGNADO, // evento nuevo, puedes crearlo
                null,
                Map.of("role", request.getRole(), "tenantId", adminTenantId.toString()),
                adminTenantId,
                AuditContext.getActorId(),
                AuditContext.getActorName(),
                AuditContext.getClientIp(),
                null
        );
    }
}

