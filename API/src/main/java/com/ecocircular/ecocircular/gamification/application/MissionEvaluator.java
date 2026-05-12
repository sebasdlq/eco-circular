package com.ecocircular.ecocircular.gamification.application;

import com.ecocircular.ecocircular.gamification.application.port.UserRecyclingActivity;
import com.ecocircular.ecocircular.gamification.domain.Mission;
import com.ecocircular.ecocircular.gamification.domain.MissionStatus;
import com.ecocircular.ecocircular.gamification.domain.UserMission;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class MissionEvaluator {

    private static final List<Mission> MISSION_CATALOG = List.of(
            new Mission(UUID.fromString("20000000-0000-0000-0000-000000000001"),
                    "Primer Paso Verde", "Realiza tu primera entrega de reciclaje",
                    "DELIVERIES", 1, 50),
            new Mission(UUID.fromString("20000000-0000-0000-0000-000000000002"),
                    "Reciclador de la Semana", "Recicla 5 kg esta semana",
                    "KG_RECYCLED", 5, 100),
            new Mission(UUID.fromString("20000000-0000-0000-0000-000000000003"),
                    "Explorador Verde", "Visita 3 puntos verdes distintos",
                    "GREEN_POINT_VISITS", 3, 75),
            new Mission(UUID.fromString("20000000-0000-0000-0000-000000000004"),
                    "Diversidad Material", "Recicla 3 tipos de materiales distintos",
                    "MATERIALS_VARIETY", 3, 80)
    );

    public List<UserMission> evaluate(UserRecyclingActivity activity) {
        LocalDate deadline = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        List<UserMission> result = new ArrayList<>();

        for (Mission mission : MISSION_CATALOG) {
            double current = resolveCurrentValue(mission, activity);
            double target = mission.getTargetValue();
            int pct = (int) Math.min(100, Math.round((current / target) * 100));
            MissionStatus status = current >= target ? MissionStatus.COMPLETADA : MissionStatus.EN_PROGRESO;

            result.add(new UserMission(
                    mission.getId(),
                    activity.getUserId(),
                    mission.getName(),
                    mission.getDescription(),
                    current,
                    target,
                    status,
                    pct,
                    mission.getRewardPoints(),
                    deadline
            ));
        }
        return result;
    }

    private double resolveCurrentValue(Mission mission, UserRecyclingActivity activity) {
        return switch (mission.getTargetType()) {
            case "DELIVERIES" -> Math.min(activity.getTotalDeliveries(), mission.getTargetValue());
            case "KG_RECYCLED" -> Math.min(activity.getTotalKgRecycled(), mission.getTargetValue());
            case "GREEN_POINT_VISITS" -> Math.min(activity.getGreenPointVisits(), mission.getTargetValue());
            case "MATERIALS_VARIETY" -> Math.min(activity.getMaterialsRecycled().size(), mission.getTargetValue());
            default -> 0.0;
        };
    }
}
