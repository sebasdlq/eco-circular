package com.ecocircular.ecocircular.iam.application;

import com.ecocircular.ecocircular.iam.domain.Tenant;
import com.ecocircular.ecocircular.iam.domain.User;
import com.ecocircular.ecocircular.iam.domain.UserTenantRole;
import com.ecocircular.ecocircular.iam.infrastructure.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserTenantRoleRepository userTenantRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));
        //No Hashea

        if (!Objects.equals(password, user.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }
//        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
//            throw new RuntimeException("Credenciales inválidas");
//        }

        List<UserTenantRole> roles = userTenantRoleRepository.findByUserId(user.getId());

        Tenant tenant;
        // Filtrar por tenant si se especifica, o tomar el active_tenant_id
        UserTenantRole currentRole = roles.stream()
                .filter(r -> r.getTenant().getId().equals(user.getActiveTenant().getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Usuario sin acceso a este tenant"));

        List<String> roleList = List.of(currentRole.getRole());
        List<String> permissionList = getPermissionsForRole(currentRole.getRole());

        return jwtService.generateToken(email, user.getId(), user.getActiveTenant().getId(), roleList, permissionList);
    }

    private List<String> getPermissionsForRole(String role) {
        // Mapeo básico según la jerarquía del PDF
        return switch (role) {
            case "ROLE_MUNICIPALITY_ADMIN" -> List.of("CREATE_CAMPAIGN", "MANAGE_GREEN_POINTS",
                    "VIEW_ALL_ANALYTICS", "MANAGE_ZONES", "VIEW_AUDIT_TRAIL", "EXPORT_REPORTS");
            case "ROLE_GREEN_POINT_OPERATOR" -> List.of("VALIDATE_DELIVERY", "VIEW_LOCAL_HISTORY",
                    "REPORT_INCIDENT", "MANAGE_BATCH");
            case "ROLE_RECYCLER" -> List.of("RECEIVE_BATCH", "REPORT_PROCESSING",
                    "VIEW_DEMAND_FORECAST", "UPDATE_MATERIAL_DEMAND");
            case "ROLE_CITIZEN" -> List.of("INIT_DELIVERY", "VIEW_OWN_IMPACT", "VIEW_RANKING", "JOIN_MISSIONS");
            default -> List.of();
        };
    }
}