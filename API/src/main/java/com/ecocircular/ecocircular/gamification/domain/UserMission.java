package com.ecocircular.ecocircular.gamification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserMission {
    private final UUID missionId;
    private final UUID userId;
    private final String missionName;
    private final String description;
    private final double currentValue;
    private final double targetValue;
    private final MissionStatus status;
    private final int progressPercent;
    private final int rewardPoints;
    private final LocalDate deadline;
}
