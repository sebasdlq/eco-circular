package com.ecocircular.ecocircular.gamification.application;

import com.ecocircular.ecocircular.gamification.application.port.UserRecyclingActivity;
import com.ecocircular.ecocircular.gamification.domain.Recommendation;
import com.ecocircular.ecocircular.gamification.domain.UserLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RecommendationEngine {

    private final LevelCalculator levelCalculator;

    public List<Recommendation> generate(UserRecyclingActivity activity) {
        List<Recommendation> recs = new ArrayList<>();
        UserLevel level = levelCalculator.calculate(activity.getTotalPoints());

        if (activity.getTotalDeliveries() == 0) {
            recs.add(new Recommendation("FREQUENCY_TIP",
                    "Realiza tu primera entrega para comenzar a ganar puntos y subir de nivel.", 1));
            return recs;
        }

        if (activity.getLastDeliveryDate() != null
                && activity.getLastDeliveryDate().isBefore(LocalDate.now().minusDays(14))) {
            recs.add(new Recommendation("FREQUENCY_TIP",
                    "Han pasado más de 2 semanas sin reciclar. ¡Es momento de volver!", 1));
        }

        if (activity.getGreenPointVisits() < 3) {
            recs.add(new Recommendation("GREENPOINT_TIP",
                    "Visita al menos 3 puntos verdes distintos para ganar el badge Explorador Verde.", 2));
        }

        if (!activity.getMaterialsRecycled().containsKey("Papel y Cartón")) {
            recs.add(new Recommendation("MATERIAL_TIP",
                    "Considera reciclar papel y cartón: genera 8 puntos por kg reciclado.", 2));
        }

        if (!activity.getMaterialsRecycled().containsKey("Metal (Aluminio)")) {
            recs.add(new Recommendation("MATERIAL_TIP",
                    "El aluminio genera 20 puntos por kg y evita 4 kg de CO2. ¡Vale la pena incluirlo!", 2));
        }

        if (level == UserLevel.BRONCE) {
            recs.add(new Recommendation("LEVEL_TIP",
                    "Necesitas " + levelCalculator.pointsToNextLevel(activity.getTotalPoints())
                            + " puntos más para alcanzar el nivel Plata.", 3));
        } else if (level == UserLevel.PLATA) {
            recs.add(new Recommendation("LEVEL_TIP",
                    "Necesitas " + levelCalculator.pointsToNextLevel(activity.getTotalPoints())
                            + " puntos más para alcanzar el nivel Oro.", 3));
        } else if (level == UserLevel.ORO) {
            recs.add(new Recommendation("LEVEL_TIP",
                    "Necesitas " + levelCalculator.pointsToNextLevel(activity.getTotalPoints())
                            + " puntos más para alcanzar el nivel Platino.", 3));
        }

        if (activity.getTotalDeliveries() >= 5 && activity.getTotalDeliveries() < 10) {
            recs.add(new Recommendation("MISSION_TIP",
                    "Estás a " + (10 - activity.getTotalDeliveries())
                            + " entregas del badge Reciclador Frecuente. ¡Sigue así!", 2));
        }

        return recs;
    }
}
