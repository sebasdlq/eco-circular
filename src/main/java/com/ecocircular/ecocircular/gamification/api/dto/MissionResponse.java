package com.ecocircular.ecocircular.gamification.api.dto;

import com.ecocircular.ecocircular.gamification.domain.Mission;
import com.ecocircular.ecocircular.gamification.domain.MissionStatus;
import com.ecocircular.ecocircular.gamification.domain.UserMission;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class MissionResponse {
    private final UUID          missionId;
    private final String        name;
    private final String        description;
    private final MissionStatus status;
    private final int           progressPercent;
    private final double        currentValue;
    private final double        targetValue;
    private final int           rewardPoints;
    private final LocalDate     deadline;

    public static MissionResponse from(UserMission um) {
        return new MissionResponse(
                um.getMission().getId(),
                um.getMission().getName(),
                um.getMission().getDescription(),
                um.getStatus(),
                um.getProgressPercent(),
                um.getCurrentValue(),
                um.getMission().getTargetValue(),
                um.getMission().getRewardPoints(),
                um.getDeadline()
        );
    }
    public static MissionResponse fromEntity(Mission m) {
        return new MissionResponse(
                m.getId(),
                m.getName(),
                m.getDescription(),
                null,  // status no aplica para el catálogo
                0,
                0,
                m.getTargetValue(),
                m.getRewardPoints(),
                m.getDeadline()
        );
    }
}
