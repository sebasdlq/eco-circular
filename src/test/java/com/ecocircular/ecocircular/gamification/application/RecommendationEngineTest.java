package com.ecocircular.ecocircular.gamification.application;

import com.ecocircular.ecocircular.gamification.application.port.UserRecyclingActivity;
import com.ecocircular.ecocircular.gamification.domain.Recommendation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationEngineTest {

    private final LevelCalculator levelCalculator = new LevelCalculator();
    private final RecommendationEngine engine = new RecommendationEngine(levelCalculator);

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID USER_ID   = UUID.randomUUID();

    // ── Rule 1: no deliveries ───────────────────────────────────────────────

    @Test
    void generate_noDeliveries_returnsSingleFirstDeliveryTip() {
        UserRecyclingActivity activity = activity(0, 0.0, 0.0, 0.0, Map.of(), 0, null);
        List<Recommendation> recs = engine.generate(activity);

        assertThat(recs).hasSize(1);
        assertThat(recs.get(0).getType()).isEqualTo("FREQUENCY_TIP");
        assertThat(recs.get(0).getPriority()).isEqualTo(1);
    }

    // ── Rule 2: inactive > 14 days ──────────────────────────────────────────

    @Test
    void generate_inactiveSince15DaysAgo_includesFrequencyTip() {
        LocalDate oldDate = LocalDate.now().minusDays(15);
        UserRecyclingActivity activity = activity(3, 5.0, 50.0, 2.0, Map.of("Plástico PET", 5.0), 1, oldDate);

        List<String> types = types(engine.generate(activity));
        assertThat(types).contains("FREQUENCY_TIP");
    }

    @Test
    void generate_activeRecently_doesNotIncludeFrequencyTip() {
        LocalDate recentDate = LocalDate.now().minusDays(5);
        UserRecyclingActivity activity = activity(3, 5.0, 50.0, 2.0, Map.of("Plástico PET", 5.0), 1, recentDate);

        List<String> types = types(engine.generate(activity));
        assertThat(types).doesNotContain("FREQUENCY_TIP");
    }

    // ── Rule 3: few green point visits ─────────────────────────────────────

    @Test
    void generate_lessThanThreeGreenPointVisits_includesGreenPointTip() {
        UserRecyclingActivity activity = activity(3, 5.0, 50.0, 2.0, Map.of("Plástico PET", 5.0), 2, LocalDate.now());
        assertThat(types(engine.generate(activity))).contains("GREENPOINT_TIP");
    }

    @Test
    void generate_threeOrMoreGreenPointVisits_doesNotIncludeGreenPointTip() {
        UserRecyclingActivity activity = activity(3, 5.0, 50.0, 2.0, Map.of("Plástico PET", 5.0), 3, LocalDate.now());
        assertThat(types(engine.generate(activity))).doesNotContain("GREENPOINT_TIP");
    }

    // ── Rule 4 & 5: missing materials ──────────────────────────────────────

    @Test
    void generate_noPaperRecycled_includesMaterialTip() {
        UserRecyclingActivity activity = activity(2, 5.0, 40.0, 2.0, Map.of("Plástico PET", 5.0), 1, LocalDate.now());
        assertThat(types(engine.generate(activity))).contains("MATERIAL_TIP");
    }

    @Test
    void generate_noAluminumRecycled_includesMaterialTip() {
        UserRecyclingActivity activity = activity(2, 5.0, 40.0, 2.0,
                Map.of("Papel y Cartón", 5.0), 1, LocalDate.now());
        assertThat(types(engine.generate(activity))).contains("MATERIAL_TIP");
    }

    // ── Rule 6: level tips ──────────────────────────────────────────────────

    @Test
    void generate_bronceLevelUser_includesLevelTip() {
        UserRecyclingActivity activity = activity(2, 5.0, 50.0, 2.0,
                Map.of("Papel y Cartón", 3.0, "Metal (Aluminio)", 2.0), 3, LocalDate.now());
        List<Recommendation> levelTips = engine.generate(activity).stream()
                .filter(r -> r.getType().equals("LEVEL_TIP")).toList();
        assertThat(levelTips).isNotEmpty();
        assertThat(levelTips.get(0).getMessage()).contains("Plata");
    }

    @Test
    void generate_plataLevelUser_levelTipMentionsOro() {
        UserRecyclingActivity activity = activity(5, 20.0, 200.0, 10.0,
                Map.of("Papel y Cartón", 10.0, "Metal (Aluminio)", 10.0), 3, LocalDate.now());
        List<Recommendation> levelTips = engine.generate(activity).stream()
                .filter(r -> r.getType().equals("LEVEL_TIP")).toList();
        assertThat(levelTips).isNotEmpty();
        assertThat(levelTips.get(0).getMessage()).contains("Oro");
    }

    @Test
    void generate_platinoLevel_noLevelTip() {
        UserRecyclingActivity activity = activity(20, 100.0, 2000.0, 80.0,
                Map.of("Papel y Cartón", 50.0, "Metal (Aluminio)", 50.0), 5, LocalDate.now());
        assertThat(types(engine.generate(activity))).doesNotContain("LEVEL_TIP");
    }

    // ── Rule 7: close to Reciclador Frecuente badge ─────────────────────────

    @Test
    void generate_sevenDeliveries_includesMissionTip() {
        UserRecyclingActivity activity = activity(7, 20.0, 150.0, 6.0,
                Map.of("Papel y Cartón", 10.0, "Metal (Aluminio)", 10.0), 3, LocalDate.now());
        assertThat(types(engine.generate(activity))).contains("MISSION_TIP");
    }

    @Test
    void generate_tenDeliveries_doesNotIncludeMissionTip() {
        UserRecyclingActivity activity = activity(10, 30.0, 300.0, 12.0,
                Map.of("Papel y Cartón", 15.0, "Metal (Aluminio)", 15.0), 3, LocalDate.now());
        assertThat(types(engine.generate(activity))).doesNotContain("MISSION_TIP");
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

    private List<String> types(List<Recommendation> recs) {
        return recs.stream().map(Recommendation::getType).toList();
    }
}
