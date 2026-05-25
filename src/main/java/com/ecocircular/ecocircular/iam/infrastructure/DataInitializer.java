package com.ecocircular.ecocircular.iam.infrastructure;

import com.ecocircular.ecocircular.iam.domain.Tenant;
import com.ecocircular.ecocircular.iam.domain.User;
import com.ecocircular.ecocircular.iam.domain.UserTenantRole;
import com.ecocircular.ecocircular.iam.infrastructure.persistence.TenantRepository;
import com.ecocircular.ecocircular.iam.infrastructure.persistence.UserRepository;
import com.ecocircular.ecocircular.iam.infrastructure.persistence.UserTenantRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final String SUPER_USER_EMAIL  = "jefe@eco.com";
    private static final String SUPER_USER_PASS   = "1234";
    private static final String ADMIN_TENANT_NAME = "ADMIN";
    private static final String ADMIN_ROLE        = "ROLE_MUNICIPALITY_ADMIN";

    private final UserRepository           userRepository;
    private final TenantRepository         tenantRepository;
    private final UserTenantRoleRepository userTenantRoleRepository;
    private final PasswordEncoder          passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Tenant adminTenant = resolveAdminTenant();
        User   superUser   = resolveSuperUser(adminTenant);
        resolveAdminRole(superUser, adminTenant);
        resolveAdmin2Role(superUser, adminTenant);
    }

    // ------------------------------------------------------------------ //
    //  Tenant                                                              //
    // ------------------------------------------------------------------ //

    private Tenant resolveAdminTenant() {
        return tenantRepository.findByNameAndType(ADMIN_TENANT_NAME, "ADMIN")
                .orElseGet(() -> {
                    Tenant tenant = new Tenant();
                    tenant.setName(ADMIN_TENANT_NAME);
                    tenant.setType("ADMIN");
                    tenant.setActive(true);
                    tenant.setCreatedAt(LocalDateTime.now());
                    Tenant saved = tenantRepository.save(tenant);
                    log.info("[DataInitializer] Tenant ADMIN creado: {}", saved.getId());
                    return saved;
                });
    }

    // ------------------------------------------------------------------ //
    //  Usuario                                                             //
    // ------------------------------------------------------------------ //

    private User resolveSuperUser(Tenant adminTenant) {
        return userRepository.findByEmail(SUPER_USER_EMAIL)
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(SUPER_USER_EMAIL);
                    user.setDisplayName("Super Admin");
                    user.setPasswordHash(SUPER_USER_PASS);
                    user.setActiveTenant(adminTenant);
                    user.setCreatedAt(LocalDateTime.now());
                    User saved = userRepository.save(user);
                    log.info("[DataInitializer] Superusuario creado: {}", saved.getId());
                    return saved;
                });
    }

    // ------------------------------------------------------------------ //
    //  Rol                                                                 //
    // ------------------------------------------------------------------ //

    private void resolveAdminRole(User user, Tenant tenant) {
        boolean roleExists = userTenantRoleRepository
                .existsByUserAndTenantAndRole(user, tenant, ADMIN_ROLE);

        if (!roleExists) {
            UserTenantRole role = new UserTenantRole();
            role.setUser(user);
            role.setTenant(tenant);
            role.setRole(ADMIN_ROLE);
            role.setAssignedAt(LocalDateTime.now());
            role.setAssignedBy(user.getId()); // se auto-asigna
            userTenantRoleRepository.save(role);
            log.info("[DataInitializer] Rol {} asignado a {}", ADMIN_ROLE, user.getEmail());
        } else {
            log.info("[DataInitializer] Setup ya existente — sin cambios.");
        }
    }

    private void resolveAdmin2Role(User user, Tenant tenant) {
        boolean roleExists = userTenantRoleRepository
                .existsByUserAndTenantAndRole(user, tenant, "ADMIN");

        if (!roleExists) {
            UserTenantRole role = new UserTenantRole();
            role.setUser(user);
            role.setTenant(tenant);
            role.setRole("ADMIN");
            role.setAssignedAt(LocalDateTime.now());
            role.setAssignedBy(user.getId()); // se auto-asigna
            userTenantRoleRepository.save(role);
            log.info("[DataInitializer] Rol {} asignado a {}", "ADMIN", user.getEmail());
        } else {
            log.info("[DataInitializer] Setup ya existente — sin cambios.");
        }
    }
}