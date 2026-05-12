package com.ecocircular.ecocircular.gamification.api.dto;

import com.ecocircular.ecocircular.gamification.domain.UserGamificationSummary;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserGamificationResponse {
    private final UUID userId;
    private final String displayName;
    private final String level;
    private final String levelDisplayName;
    private final double totalPoints;
    private final double totalCo2AvoidedKg;
    private final double totalKgRecycled;
    private final int totalDeliveries;
    private final int badgesEarned;
    private final int activeMissions;
    private final int pointsToNextLevel;

    public static UserGamificationResponse from(UserGamificationSummary s) {
        return new UserGamificationResponse(
                s.getUserId(), s.getDisplayName(),
                s.getLevel().name(), s.getLevelDisplayName(),
                s.getTotalPoints(), s.getTotalCo2AvoidedKg(),
                s.getTotalKgRecycled(), s.getTotalDeliveries(),
                s.getBadgesEarned(), s.getActiveMissions(),
                s.getPointsToNextLevel()
        );
    }
}
