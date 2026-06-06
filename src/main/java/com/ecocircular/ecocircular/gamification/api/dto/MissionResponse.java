package com.ecocircular.ecocircular.gamification.api.dto;

import com.ecocircular.ecocircular.gamification.domain.MissionStatus;
import com.ecocircular.ecocircular.gamification.domain.UserMission;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class MissionResponse {
    private final UUID missionId;
    private final String name;
    private final String description;
    private final MissionStatus status;
    private final int progressPercent;
    private final double currentValue;
    private final double targetValue;
    private final int rewardPoints;
    private final LocalDate deadline;

    public static MissionResponse from(UserMission um) {
        return new MissionResponse(
                um.getMissionId(), um.getMissionName(), um.getDescription(),
                um.getStatus(), um.getProgressPercent(),
                um.getCurrentValue(), um.getTargetValue(),
                um.getRewardPoints(), um.getDeadline()
        );
    }
}
