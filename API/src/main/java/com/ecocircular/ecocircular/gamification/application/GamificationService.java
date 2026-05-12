package com.ecocircular.ecocircular.gamification.application;

import com.ecocircular.ecocircular.gamification.application.port.GamificationActivityPort;
import com.ecocircular.ecocircular.gamification.application.port.UserRecyclingActivity;
import com.ecocircular.ecocircular.gamification.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private final GamificationActivityPort activityPort;
    private final LevelCalculator levelCalculator;
    private final BadgeEvaluator badgeEvaluator;
    private final MissionEvaluator missionEvaluator;
    private final RecommendationEngine recommendationEngine;
    private final RankingService rankingService;

    public UserGamificationSummary getSummary(UUID tenantId, UUID userId) {
        UserRecyclingActivity activity = activityPort.getUserRecyclingActivity(tenantId, userId);
        UserLevel level = levelCalculator.calculate(activity.getTotalPoints());
        int badgesEarned = badgeEvaluator.evaluate(activity).size();
        long activeMissions = missionEvaluator.evaluate(activity).stream()
                .filter(m -> m.getStatus() == MissionStatus.EN_PROGRESO)
                .count();

        return new UserGamificationSummary(
                activity.getUserId(),
                activity.getDisplayName(),
                level,
                level.getDisplayName(),
                activity.getTotalPoints(),
                activity.getTotalCo2AvoidedKg(),
                activity.getTotalKgRecycled(),
                activity.getTotalDeliveries(),
                badgesEarned,
                (int) activeMissions,
                levelCalculator.pointsToNextLevel(activity.getTotalPoints())
        );
    }

    public List<UserBadge> getBadges(UUID tenantId, UUID userId) {
        return badgeEvaluator.evaluate(activityPort.getUserRecyclingActivity(tenantId, userId));
    }

    public List<UserMission> getMissions(UUID tenantId, UUID userId) {
        return missionEvaluator.evaluate(activityPort.getUserRecyclingActivity(tenantId, userId));
    }

    public List<Recommendation> getRecommendations(UUID tenantId, UUID userId) {
        return recommendationEngine.generate(activityPort.getUserRecyclingActivity(tenantId, userId));
    }

    public List<RankingEntry> getRanking(UUID tenantId, RankingScope scope) {
        return rankingService.getRanking(tenantId, scope);
    }
}
