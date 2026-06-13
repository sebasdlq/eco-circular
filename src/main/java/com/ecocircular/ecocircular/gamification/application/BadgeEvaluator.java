package com.ecocircular.ecocircular.gamification.application;

import com.ecocircular.ecocircular.common.audit.AuditContext;
import com.ecocircular.ecocircular.common.audit.AuditEvents;
import com.ecocircular.ecocircular.common.audit.AuditService;
import com.ecocircular.ecocircular.gamification.api.dto.BadgeResponse;
import com.ecocircular.ecocircular.gamification.application.port.UserRecyclingActivity;
import com.ecocircular.ecocircular.gamification.domain.Badge;
import com.ecocircular.ecocircular.gamification.domain.UserBadge;
import com.ecocircular.ecocircular.gamification.infrastructure.persistence.BadgeRepository;
import com.ecocircular.ecocircular.gamification.infrastructure.persistence.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@RequiredArgsConstructor
public class BadgeEvaluator {

    private final BadgeRepository     badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final AuditService        auditService;

    /**
     * Evalúa qué badges merece el usuario, persiste los nuevos
     * y devuelve la lista completa de badges ganados en el tenant.
     */
    @Transactional
    public List<BadgeResponse> evaluate(UserRecyclingActivity activity, UUID tenantId) {
        List<Badge> catalog = badgeRepository.findAvailableForTenant(tenantId);

        log.info("[BadgeEvaluator] Tenant: {} | Catálogo: {} badges | Deliveries del usuario: {}",
                tenantId, catalog.size(), activity.getTotalDeliveries());

        for (Badge badge : catalog) {
            boolean yaLoTiene = userBadgeRepository.existsByUserIdAndBadge_IdAndTenantId(
                    activity.getUserId(), badge.getId(), tenantId);
            boolean merece = merece(badge, activity);

            log.info("[BadgeEvaluator] Badge: '{}' | iconCode: '{}' | yaLoTiene: {} | merece: {}",
                    badge.getName(), badge.getIconCode(), yaLoTiene, merece);

            if (!yaLoTiene && merece) {
                UserBadge userBadge = new UserBadge(
                        activity.getUserId(), badge, LocalDateTime.now());
                userBadge.setTenantId(tenantId);
                userBadgeRepository.save(userBadge);
                log.info("[BadgeEvaluator] ✅ Badge otorgado: {}", badge.getName());
            }
        }

        return userBadgeRepository.findByUserIdAndTenantId(activity.getUserId(), tenantId)
                .stream().map(BadgeResponse::from).toList();
    }
    /**
     * Lógica de elegibilidad por badge.
     * Para agregar un nuevo badge: agregar su iconCode al switch y definir la condición.
     */
    private boolean merece(Badge badge, UserRecyclingActivity a) {
        return switch (badge.getIconCode()) {
            case "RECYCLE_1"   -> a.getTotalDeliveries() >= 1;
            case "RECYCLE_10"  -> a.getTotalDeliveries() >= 10;
            case "EARTH_50KG"  -> a.getTotalKgRecycled() >= 50.0;
            case "CO2_25KG"    -> a.getTotalCo2AvoidedKg() >= 25.0;
            case "MATERIALS_3" -> a.getMaterialsRecycled().size() >= 3;
            case "GOLD_LEVEL"  -> a.getTotalPoints() >= 500.0;
            default            -> false; // badges sin lógica hardcodeada no se otorgan automáticamente
        };
    }

    record BadgeSnapshot(UUID userId, UUID badgeId, String badgeName) {}
}
