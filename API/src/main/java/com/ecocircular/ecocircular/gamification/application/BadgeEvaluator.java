package com.ecocircular.ecocircular.gamification.application;

import com.ecocircular.ecocircular.gamification.application.port.UserRecyclingActivity;
import com.ecocircular.ecocircular.gamification.domain.Badge;
import com.ecocircular.ecocircular.gamification.domain.UserBadge;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class BadgeEvaluator {

    private static final Badge PRIMER_RECICLAJE = new Badge(
            UUID.fromString("10000000-0000-0000-0000-000000000001"),
            "Primer Reciclaje", "Completaste tu primera entrega de reciclaje", "RECYCLE_1");

    private static final Badge RECICLADOR_FRECUENTE = new Badge(
            UUID.fromString("10000000-0000-0000-0000-000000000002"),
            "Reciclador Frecuente", "Completaste 10 entregas de reciclaje", "RECYCLE_10");

    private static final Badge GUARDIAN_PLANETA = new Badge(
            UUID.fromString("10000000-0000-0000-0000-000000000003"),
            "Guardián del Planeta", "Reciclaste 50 kg en total", "EARTH_50KG");

    private static final Badge HEROE_CO2 = new Badge(
            UUID.fromString("10000000-0000-0000-0000-000000000004"),
            "Héroe del CO2", "Evitaste 25 kg de emisiones de CO2", "CO2_25KG");

    private static final Badge EXPLORADOR_MATERIALES = new Badge(
            UUID.fromString("10000000-0000-0000-0000-000000000005"),
            "Explorador de Materiales", "Reciclaste 3 tipos distintos de materiales", "MATERIALS_3");

    private static final Badge NIVEL_ORO = new Badge(
            UUID.fromString("10000000-0000-0000-0000-000000000006"),
            "Nivel Oro", "Alcanzaste el nivel Oro con 500 puntos", "GOLD_LEVEL");

    public List<UserBadge> evaluate(UserRecyclingActivity activity) {
        List<UserBadge> earned = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        UUID userId = activity.getUserId();

        if (activity.getTotalDeliveries() >= 1)
            earned.add(new UserBadge(userId, PRIMER_RECICLAJE, now));

        if (activity.getTotalDeliveries() >= 10)
            earned.add(new UserBadge(userId, RECICLADOR_FRECUENTE, now));

        if (activity.getTotalKgRecycled() >= 50.0)
            earned.add(new UserBadge(userId, GUARDIAN_PLANETA, now));

        if (activity.getTotalCo2AvoidedKg() >= 25.0)
            earned.add(new UserBadge(userId, HEROE_CO2, now));

        if (activity.getMaterialsRecycled().size() >= 3)
            earned.add(new UserBadge(userId, EXPLORADOR_MATERIALES, now));

        if (activity.getTotalPoints() >= 500.0)
            earned.add(new UserBadge(userId, NIVEL_ORO, now));

        return earned;
    }

    public static List<Badge> getAllBadgeDefinitions() {
        return List.of(PRIMER_RECICLAJE, RECICLADOR_FRECUENTE, GUARDIAN_PLANETA,
                HEROE_CO2, EXPLORADOR_MATERIALES, NIVEL_ORO);
    }
}
