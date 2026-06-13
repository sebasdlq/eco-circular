package com.ecocircular.ecocircular.gamification.application;

import com.ecocircular.ecocircular.gamification.api.dto.BadgeResponse;
import com.ecocircular.ecocircular.gamification.api.dto.MissionResponse;
import com.ecocircular.ecocircular.gamification.application.port.GamificationActivityPort;
import com.ecocircular.ecocircular.gamification.application.port.UserRecyclingActivity;
import com.ecocircular.ecocircular.gamification.domain.*;
import com.ecocircular.ecocircular.gamification.infrastructure.persistence.UserBadgeRepository;
import com.ecocircular.ecocircular.gamification.infrastructure.persistence.UserMissionRepository;
import com.ecocircular.ecocircular.recyclingops.event.EntregaValidadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GamificationService {

    private final GamificationActivityPort activityPort;
    private final LevelCalculator          levelCalculator;
    private final BadgeEvaluator           badgeEvaluator;
    private final MissionEvaluator         missionEvaluator;
    private final RecommendationEngine     recommendationEngine;
    private final RankingService           rankingService;
    private final UserBadgeRepository userBadgeRepository;
    private final UserMissionRepository userMissionRepository;

    // ── Listener del evento de entrega validada ───────────────────────────────

    /**
     * Se dispara automáticamente cuando DeliveryService valida una entrega.
     * @Async para no bloquear la respuesta HTTP de la validación.
     */
    @Async
    @EventListener
    public void onEntregaValidada(EntregaValidadaEvent event) {
        log.info("[Gamification] Evaluando badges y misiones para usuario {} en tenant {}",
                event.userId(), event.tenantId());
        try {
            UserRecyclingActivity activity = activityPort
                    .getUserRecyclingActivity(event.tenantId(), event.userId());
            badgeEvaluator.evaluate(activity, event.tenantId());
            missionEvaluator.evaluate(activity, event.tenantId());
        } catch (Exception ex) {
            log.error("[Gamification] Error evaluando usuario {}: {}", event.userId(), ex.getMessage());
        }
    }

    // ── Consultas ─────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public UserGamificationSummary getSummary(UUID tenantId, UUID userId) {
        UserRecyclingActivity activity = activityPort.getUserRecyclingActivity(tenantId, userId);
        UserLevel level = levelCalculator.calculate(activity.getTotalPoints());
        int badgesEarned    = userBadgeRepository.findByUserIdAndTenantId(userId, tenantId).size();
        long activeMissions = userMissionRepository.findByUserIdAndTenantId(userId, tenantId)
                .stream().filter(m -> m.getStatus() == MissionStatus.EN_PROGRESO).count();
        return new UserGamificationSummary(
                activity.getUserId(), activity.getDisplayName(),
                level, level.getDisplayName(),
                activity.getTotalPoints(), activity.getTotalCo2AvoidedKg(),
                activity.getTotalKgRecycled(), activity.getTotalDeliveries(),
                badgesEarned, (int) activeMissions,
                levelCalculator.pointsToNextLevel(activity.getTotalPoints())
        );
    }

    @Transactional(readOnly = true)
    public List<BadgeResponse> getBadges(UUID tenantId, UUID userId) {
        return userBadgeRepository.findByUserIdAndTenantId(userId, tenantId)
                .stream()
                .map(BadgeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MissionResponse> getMissions(UUID tenantId, UUID userId) {
        return userMissionRepository.findByUserIdAndTenantId(userId, tenantId)
                .stream()
                .map(MissionResponse::from)
                .toList();
    }

    public List<Recommendation> getRecommendations(UUID tenantId, UUID userId) {
        return recommendationEngine.generate(activityPort.getUserRecyclingActivity(tenantId, userId));
    }

    public List<RankingEntry> getRanking(UUID tenantId, RankingScope scope) {
        return rankingService.getRanking(tenantId, scope);
    }
}
