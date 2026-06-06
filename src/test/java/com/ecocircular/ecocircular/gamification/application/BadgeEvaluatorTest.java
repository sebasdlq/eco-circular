package com.ecocircular.ecocircular.gamification.application;

import com.ecocircular.ecocircular.gamification.application.port.UserRecyclingActivity;
import com.ecocircular.ecocircular.gamification.domain.UserBadge;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BadgeEvaluatorTest {

    private final BadgeEvaluator evaluator = new BadgeEvaluator();

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID USER_ID   = UUID.randomUUID();

    @Test
    void evaluate_noActivity_earnsNoBadges() {
        UserRecyclingActivity activity = activity(0, 0.0, 0.0, 0.0, Map.of(), 0, null);
        assertThat(evaluator.evaluate(activity)).isEmpty();
    }

    @Test
    void evaluate_oneDelivery_earnsPrimerReciclaje() {
        UserRecyclingActivity activity = activity(1, 1.0, 10.0, 0.5, Map.of("Plástico PET", 1.0), 1, LocalDate.now());
        List<UserBadge> badges = evaluator.evaluate(activity);
        assertThat(badgeNames(badges)).contains("Primer Reciclaje");
    }

    @Test
    void evaluate_tenDeliveries_earnsRecicladorFrecuente() {
        UserRecyclingActivity activity = activity(10, 30.0, 200.0, 10.0, Map.of("Plástico PET", 30.0), 5, LocalDate.now());
        assertThat(badgeNames(evaluator.evaluate(activity))).contains("Reciclador Frecuente");
    }

    @Test
    void evaluate_nineDeliveries_doesNotEarnRecicladorFrecuente() {
        UserRecyclingActivity activity = activity(9, 20.0, 180.0, 8.0, Map.of("Papel y Cartón", 20.0), 3, LocalDate.now());
        assertThat(badgeNames(evaluator.evaluate(activity))).doesNotContain("Reciclador Frecuente");
    }

    @Test
    void evaluate_fiftyKgRecycled_earnsGuardianPlaneta() {
        UserRecyclingActivity activity = activity(5, 50.0, 300.0, 20.0, Map.of("Vidrio", 50.0), 3, LocalDate.now());
        assertThat(badgeNames(evaluator.evaluate(activity))).contains("Guardián del Planeta");
    }

    @Test
    void evaluate_twentyFiveCo2Avoided_earnsHeroeCO2() {
        UserRecyclingActivity activity = activity(5, 40.0, 300.0, 25.0, Map.of("Metal (Aluminio)", 40.0), 2, LocalDate.now());
        assertThat(badgeNames(evaluator.evaluate(activity))).contains("Héroe del CO2");
    }

    @Test
    void evaluate_threeMaterialTypes_earnsExploradorMateriales() {
        Map<String, Double> materials = Map.of("Plástico PET", 5.0, "Papel y Cartón", 5.0, "Vidrio", 5.0);
        UserRecyclingActivity activity = activity(3, 15.0, 100.0, 5.0, materials, 2, LocalDate.now());
        assertThat(badgeNames(evaluator.evaluate(activity))).contains("Explorador de Materiales");
    }

    @Test
    void evaluate_twoMaterialTypes_doesNotEarnExploradorMateriales() {
        Map<String, Double> materials = Map.of("Plástico PET", 10.0, "Papel y Cartón", 5.0);
        UserRecyclingActivity activity = activity(3, 15.0, 100.0, 5.0, materials, 2, LocalDate.now());
        assertThat(badgeNames(evaluator.evaluate(activity))).doesNotContain("Explorador de Materiales");
    }

    @Test
    void evaluate_fiveHundredPoints_earnsNivelOro() {
        UserRecyclingActivity activity = activity(8, 60.0, 500.0, 22.0, Map.of("Plástico PET", 60.0), 4, LocalDate.now());
        assertThat(badgeNames(evaluator.evaluate(activity))).contains("Nivel Oro");
    }

    @Test
    void evaluate_highActivity_earnsAllBadges() {
        Map<String, Double> materials = Map.of("Plástico PET", 30.0, "Papel y Cartón", 20.0, "Vidrio", 15.0);
        UserRecyclingActivity activity = activity(15, 65.0, 600.0, 30.0, materials, 6, LocalDate.now());
        List<String> names = badgeNames(evaluator.evaluate(activity));
        assertThat(names).containsExactlyInAnyOrder(
                "Primer Reciclaje",
                "Reciclador Frecuente",
                "Guardián del Planeta",
                "Héroe del CO2",
                "Explorador de Materiales",
                "Nivel Oro"
        );
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private UserRecyclingActivity activity(int deliveries, double kg, double points, double co2,
                                           Map<String, Double> materials, int gpVisits, LocalDate lastDate) {
        return new UserRecyclingActivity(
                USER_ID, "Test", "CIUDADANO", TENANT_ID,
                deliveries, kg, points, co2,
                materials, gpVisits, lastDate
        );
    }

    private List<String> badgeNames(List<UserBadge> badges) {
        return badges.stream().map(b -> b.getBadge().getName()).toList();
    }
}
