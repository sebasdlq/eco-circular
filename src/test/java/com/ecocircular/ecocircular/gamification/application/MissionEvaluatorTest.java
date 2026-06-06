package com.ecocircular.ecocircular.gamification.application;

import com.ecocircular.ecocircular.gamification.application.port.UserRecyclingActivity;
import com.ecocircular.ecocircular.gamification.domain.MissionStatus;
import com.ecocircular.ecocircular.gamification.domain.UserMission;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MissionEvaluatorTest {

    private final MissionEvaluator evaluator = new MissionEvaluator();

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID USER_ID   = UUID.randomUUID();

    @Test
    void evaluate_returnsAllFourMissions() {
        List<UserMission> missions = evaluator.evaluate(zeroActivity());
        assertThat(missions).hasSize(4);
    }

    @Test
    void evaluate_deadlineIsEndOfCurrentMonth() {
        List<UserMission> missions = evaluator.evaluate(zeroActivity());
        LocalDate expected = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        missions.forEach(m -> assertThat(m.getDeadline()).isEqualTo(expected));
    }

    // ── DELIVERIES mission (target = 1) ────────────────────────────────────

    @Test
    void deliveriesMission_zeroDeliveries_isInProgress() {
        UserMission mission = findByName(evaluator.evaluate(zeroActivity()), "Primer Paso Verde");
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.EN_PROGRESO);
        assertThat(mission.getProgressPercent()).isZero();
    }

    @Test
    void deliveriesMission_oneDelivery_isCompleted() {
        UserMission mission = findByName(
                evaluator.evaluate(activity(1, 0.0, 0.0, Map.of(), 0)),
                "Primer Paso Verde");
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.COMPLETADA);
        assertThat(mission.getProgressPercent()).isEqualTo(100);
    }

    // ── KG_RECYCLED mission (target = 5 kg) ────────────────────────────────

    @Test
    void kgRecycledMission_halfProgress_correctPercent() {
        UserMission mission = findByName(
                evaluator.evaluate(activity(1, 2.5, 0.0, Map.of(), 0)),
                "Reciclador de la Semana");
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.EN_PROGRESO);
        assertThat(mission.getProgressPercent()).isEqualTo(50);
    }

    @Test
    void kgRecycledMission_atTarget_isCompleted() {
        UserMission mission = findByName(
                evaluator.evaluate(activity(1, 5.0, 0.0, Map.of(), 0)),
                "Reciclador de la Semana");
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.COMPLETADA);
        assertThat(mission.getProgressPercent()).isEqualTo(100);
    }

    @Test
    void kgRecycledMission_aboveTarget_doesNotExceed100Percent() {
        UserMission mission = findByName(
                evaluator.evaluate(activity(2, 20.0, 0.0, Map.of(), 0)),
                "Reciclador de la Semana");
        assertThat(mission.getProgressPercent()).isEqualTo(100);
    }

    // ── GREEN_POINT_VISITS mission (target = 3) ────────────────────────────

    @Test
    void greenPointMission_oneVisit_isInProgress() {
        UserMission mission = findByName(
                evaluator.evaluate(activity(1, 0.0, 0.0, Map.of(), 1)),
                "Explorador Verde");
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.EN_PROGRESO);
        assertThat(mission.getProgressPercent()).isEqualTo(33);
    }

    @Test
    void greenPointMission_threeVisits_isCompleted() {
        UserMission mission = findByName(
                evaluator.evaluate(activity(3, 0.0, 0.0, Map.of(), 3)),
                "Explorador Verde");
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.COMPLETADA);
        assertThat(mission.getProgressPercent()).isEqualTo(100);
    }

    // ── MATERIALS_VARIETY mission (target = 3 types) ───────────────────────

    @Test
    void materialsMission_twoTypes_isInProgress() {
        Map<String, Double> twoMaterials = Map.of("Plástico PET", 5.0, "Papel y Cartón", 5.0);
        UserMission mission = findByName(
                evaluator.evaluate(activity(2, 10.0, 0.0, twoMaterials, 1)),
                "Diversidad Material");
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.EN_PROGRESO);
        assertThat(mission.getProgressPercent()).isEqualTo(67);
    }

    @Test
    void materialsMission_threeTypes_isCompleted() {
        Map<String, Double> threeMaterials = Map.of("Plástico PET", 5.0, "Papel y Cartón", 5.0, "Vidrio", 3.0);
        UserMission mission = findByName(
                evaluator.evaluate(activity(3, 13.0, 0.0, threeMaterials, 2)),
                "Diversidad Material");
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.COMPLETADA);
        assertThat(mission.getProgressPercent()).isEqualTo(100);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private UserRecyclingActivity zeroActivity() {
        return activity(0, 0.0, 0.0, Map.of(), 0);
    }

    private UserRecyclingActivity activity(int deliveries, double kg, double points,
                                           Map<String, Double> materials, int gpVisits) {
        return new UserRecyclingActivity(
                USER_ID, "Test", "CIUDADANO", TENANT_ID,
                deliveries, kg, points, 0.0,
                materials, gpVisits, deliveries > 0 ? LocalDate.now() : null
        );
    }

    private UserMission findByName(List<UserMission> missions, String name) {
        return missions.stream()
                .filter(m -> m.getMissionName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Mission not found: " + name));
    }
}
