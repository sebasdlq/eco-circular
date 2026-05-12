package com.ecocircular.ecocircular.gamification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserGamificationSummary {
    private final UUID userId;
    private final String displayName;
    private final UserLevel level;
    private final String levelDisplayName;
    private final double totalPoints;
    private final double totalCo2AvoidedKg;
    private final double totalKgRecycled;
    private final int totalDeliveries;
    private final int badgesEarned;
    private final int activeMissions;
    private final int pointsToNextLevel;
}
