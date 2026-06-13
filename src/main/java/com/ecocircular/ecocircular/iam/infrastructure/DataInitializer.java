package com.ecocircular.ecocircular.iam.infrastructure;

import com.ecocircular.ecocircular.common.base.TenantContext;
import com.ecocircular.ecocircular.gamification.domain.Badge;
import com.ecocircular.ecocircular.gamification.domain.Mission;
import com.ecocircular.ecocircular.gamification.infrastructure.persistence.BadgeRepository;
import com.ecocircular.ecocircular.gamification.infrastructure.persistence.MissionRepository;
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
import java.util.List;
import java.util.UUID;

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
    private final BadgeRepository          badgeRepository;
    private final MissionRepository        missionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Tenant adminTenant = resolveAdminTenant();
        User   superUser   = resolveSuperUser(adminTenant);
        resolveAdminRole(superUser, adminTenant);
        resolveAdmin2Role(superUser, adminTenant);
        seedBadges();
        seedMissions();
    }

    // ── Tenant ────────────────────────────────────────────────────────────────

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

    // ── Usuario ───────────────────────────────────────────────────────────────

    private User resolveSuperUser(Tenant adminTenant) {
        return userRepository.findByEmail(SUPER_USER_EMAIL)
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(SUPER_USER_EMAIL);
                    user.setDisplayName("Super Admin");
                    user.setPasswordHash(passwordEncoder.encode(SUPER_USER_PASS));
                    user.setActiveTenant(adminTenant);
                    user.setCreatedAt(LocalDateTime.now());
                    User saved = userRepository.save(user);
                    log.info("[DataInitializer] Superusuario creado: {}", saved.getId());
                    return saved;
                });
    }

    // ── Roles ─────────────────────────────────────────────────────────────────

    private void resolveAdminRole(User user, Tenant tenant) {
        if (!userTenantRoleRepository.existsByUserAndTenantAndRole(user, tenant, ADMIN_ROLE)) {
            UserTenantRole role = new UserTenantRole();
            role.setUser(user);
            role.setTenant(tenant);
            role.setRole(ADMIN_ROLE);
            role.setAssignedAt(LocalDateTime.now());
            role.setAssignedBy(user.getId());
            userTenantRoleRepository.save(role);
            log.info("[DataInitializer] Rol {} asignado a {}", ADMIN_ROLE, user.getEmail());
        } else {
            log.info("[DataInitializer] Setup ya existente — sin cambios.");
        }
    }

    private void resolveAdmin2Role(User user, Tenant tenant) {
        if (!userTenantRoleRepository.existsByUserAndTenantAndRole(user, tenant, "ADMIN")) {
            UserTenantRole role = new UserTenantRole();
            role.setUser(user);
            role.setTenant(tenant);
            role.setRole("ADMIN");
            role.setAssignedAt(LocalDateTime.now());
            role.setAssignedBy(user.getId());
            userTenantRoleRepository.save(role);
            log.info("[DataInitializer] Rol ADMIN asignado a {}", user.getEmail());
        } else {
            log.info("[DataInitializer] Setup ya existente — sin cambios.");
        }
    }

    // ── Badges semilla ────────────────────────────────────────────────────────

    private void seedBadges() {
        List<BadgeSeed> seeds = List.of(
                new BadgeSeed("Primer Reciclaje",         "Completaste tu primera entrega de reciclaje",     "RECYCLE_1"),
                new BadgeSeed("Reciclador Frecuente",     "Completaste 10 entregas de reciclaje",             "RECYCLE_10"),
                new BadgeSeed("Guardián del Planeta",     "Reciclaste 50 kg en total",                        "EARTH_50KG"),
                new BadgeSeed("Héroe del CO2",            "Evitaste 25 kg de emisiones de CO2",               "CO2_25KG"),
                new BadgeSeed("Explorador de Materiales", "Reciclaste 3 tipos distintos de materiales",       "MATERIALS_3"),
                new BadgeSeed("Nivel Oro",                "Alcanzaste el nivel Oro con 500 puntos",           "GOLD_LEVEL")
        );

        for (BadgeSeed seed : seeds) {
            boolean exists = badgeRepository.findAll().stream()
                    .anyMatch(b -> b.getIconCode().equals(seed.iconCode()));

            if (!exists) {
                // Limpiar TenantContext para que prePersist no asigne tenant_id
                UUID tenantAnterior = TenantContext.getTenantId();
                TenantContext.clear();

                Badge badge = new Badge(seed.name(), seed.description(), seed.iconCode());
                badge.setTenantId(null);
                badgeRepository.saveAndFlush(badge);

                // Restaurar TenantContext
                if (tenantAnterior != null) TenantContext.setTenantId(tenantAnterior);

                log.info("[DataInitializer] Badge semilla creado: {}", seed.name());
            }
        }
    }

    // ── Misiones semilla ──────────────────────────────────────────────────────

    private void seedMissions() {
        List<MissionSeed> seeds = List.of(
                new MissionSeed("Primer Paso Verde",       "Realiza tu primera entrega de reciclaje",  "DELIVERIES",         1,  50),
                new MissionSeed("Reciclador de la Semana", "Recicla 5 kg",                             "KG_RECYCLED",        5,  100),
                new MissionSeed("Explorador Verde",        "Visita 3 puntos verdes distintos",          "GREEN_POINT_VISITS", 3,  75),
                new MissionSeed("Diversidad Material",     "Recicla 3 tipos de materiales distintos",  "MATERIALS_VARIETY",  3,  80)
        );

        for (MissionSeed seed : seeds) {
            boolean exists = missionRepository.findAll().stream()
                    .anyMatch(m -> m.getName().equals(seed.name()));

            if (!exists) {
                UUID tenantAnterior = TenantContext.getTenantId();
                TenantContext.clear();

                Mission mission = new Mission(
                        seed.name(), seed.description(),
                        seed.targetType(), seed.targetValue(),
                        seed.rewardPoints(), null);
                mission.setTenantId(null);
                missionRepository.saveAndFlush(mission);

                if (tenantAnterior != null) TenantContext.setTenantId(tenantAnterior);

                log.info("[DataInitializer] Misión semilla creada: {}", seed.name());
            }
        }
    }

    record BadgeSeed(String name, String description, String iconCode) {}
    record MissionSeed(String name, String description, String targetType,
                       double targetValue, int rewardPoints) {}
}