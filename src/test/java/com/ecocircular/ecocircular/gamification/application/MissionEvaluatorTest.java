package com.ecocircular.ecocircular.gamification.application;

import com.ecocircular.ecocircular.common.audit.AuditService;
import com.ecocircular.ecocircular.gamification.api.dto.MissionResponse;
import com.ecocircular.ecocircular.gamification.application.port.UserRecyclingActivity;
import com.ecocircular.ecocircular.gamification.domain.Mission;
import com.ecocircular.ecocircular.gamification.domain.MissionStatus;
import com.ecocircular.ecocircular.gamification.infrastructure.persistence.MissionRepository;
import com.ecocircular.ecocircular.gamification.infrastructure.persistence.UserMissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MissionEvaluatorTest {

    private static final UUID TENANT_ID = UUID.fromString("a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1");
    private static final UUID USER_ID   = UUID.fromString("b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1");

    @Mock private MissionRepository     missionRepository;
    @Mock private UserMissionRepository userMissionRepository;
    @Mock private AuditService          auditService;

    @InjectMocks
    private MissionEvaluator evaluator;

    // Misiones del catálogo semilla
    private Mission primerPasoVerde;
    private Mission recicladorSemana;
    private Mission exploradorVerde;
    private Mission diversidadMaterial;

    @BeforeEach
    void setUp() {
        primerPasoVerde    = mission("Primer Paso Verde",       "DELIVERIES",         1,  50);
        recicladorSemana   = mission("Reciclador de la Semana", "KG_RECYCLED",        5,  100);
        exploradorVerde    = mission("Explorador Verde",        "GREEN_POINT_VISITS", 3,  75);
        diversidadMaterial = mission("Diversidad Material",     "MATERIALS_VARIETY",  3,  80);

        // Por defecto el catálogo devuelve las 4 misiones
        when(missionRepository.findAvailableForTenant(TENANT_ID))
                .thenReturn(List.of(
                        primerPasoVerde, recicladorSemana,
                        exploradorVerde, diversidadMaterial
                ));

        // Por defecto no existe ningún UserMission para el usuario
        when(userMissionRepository.findByUserIdAndMission_IdAndTenantId(any(), any(), any()))
                .thenReturn(Optional.empty());

        // findByUserIdAndTenantId devuelve lista vacía por defecto
        when(userMissionRepository.findByUserIdAndTenantId(any(), any()))
                .thenReturn(List.of());
    }

    @Test
    void evaluate_returnsAllFourMissions() {
        when(userMissionRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(
                        userMission(primerPasoVerde,    MissionStatus.EN_PROGRESO, 0,  0),
                        userMission(recicladorSemana,   MissionStatus.EN_PROGRESO, 0,  0),
                        userMission(exploradorVerde,    MissionStatus.EN_PROGRESO, 0,  0),
                        userMission(diversidadMaterial, MissionStatus.EN_PROGRESO, 0,  0)
                ));

        List<MissionResponse> missions = evaluator.evaluate(zeroActivity(), TENANT_ID);
        assertThat(missions).hasSize(4);
    }

    @Test
    void evaluate_deadlineIsEndOfCurrentMonth() {
        LocalDate expected = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        when(userMissionRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(
                        userMission(primerPasoVerde,    MissionStatus.EN_PROGRESO, 0, 0),
                        userMission(recicladorSemana,   MissionStatus.EN_PROGRESO, 0, 0),
                        userMission(exploradorVerde,    MissionStatus.EN_PROGRESO, 0, 0),
                        userMission(diversidadMaterial, MissionStatus.EN_PROGRESO, 0, 0)
                ));

        evaluator.evaluate(zeroActivity(), TENANT_ID)
                .forEach(m -> assertThat(m.getDeadline()).isEqualTo(expected));
    }

    // ── DELIVERIES mission (target = 1) ──────────────────────────────────────

    @Test
    void deliveriesMission_zeroDeliveries_isInProgress() {
        when(userMissionRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(
                        userMission(primerPasoVerde, MissionStatus.EN_PROGRESO, 0, 0)
                ));

        MissionResponse mission = findByName(evaluator.evaluate(zeroActivity(), TENANT_ID), "Primer Paso Verde");
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.EN_PROGRESO);
        assertThat(mission.getProgressPercent()).isZero();
    }

    @Test
    void deliveriesMission_oneDelivery_isCompleted() {
        when(userMissionRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(
                        userMission(primerPasoVerde, MissionStatus.COMPLETADA, 1, 100)
                ));

        MissionResponse mission = findByName(
                evaluator.evaluate(activity(1, 0.0, 0.0, Map.of(), 0), TENANT_ID),
                "Primer Paso Verde");
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.COMPLETADA);
        assertThat(mission.getProgressPercent()).isEqualTo(100);
    }

    // ── KG_RECYCLED mission (target = 5 kg) ──────────────────────────────────

    @Test
    void kgRecycledMission_halfProgress_correctPercent() {
        when(userMissionRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(
                        userMission(recicladorSemana, MissionStatus.EN_PROGRESO, 2, 50)
                ));

        MissionResponse mission = findByName(
                evaluator.evaluate(activity(1, 2.5, 0.0, Map.of(), 0), TENANT_ID),
                "Reciclador de la Semana");
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.EN_PROGRESO);
        assertThat(mission.getProgressPercent()).isEqualTo(50);
    }

    @Test
    void kgRecycledMission_atTarget_isCompleted() {
        when(userMissionRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(
                        userMission(recicladorSemana, MissionStatus.COMPLETADA, 5, 100)
                ));

        MissionResponse mission = findByName(
                evaluator.evaluate(activity(1, 5.0, 0.0, Map.of(), 0), TENANT_ID),
                "Reciclador de la Semana");
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.COMPLETADA);
        assertThat(mission.getProgressPercent()).isEqualTo(100);
    }

    @Test
    void kgRecycledMission_aboveTarget_doesNotExceed100Percent() {
        when(userMissionRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(
                        userMission(recicladorSemana, MissionStatus.COMPLETADA, 5, 100)
                ));

        MissionResponse mission = findByName(
                evaluator.evaluate(activity(2, 20.0, 0.0, Map.of(), 0), TENANT_ID),
                "Reciclador de la Semana");
        assertThat(mission.getProgressPercent()).isEqualTo(100);
    }

    // ── GREEN_POINT_VISITS mission (target = 3) ───────────────────────────────

    @Test
    void greenPointMission_oneVisit_isInProgress() {
        when(userMissionRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(
                        userMission(exploradorVerde, MissionStatus.EN_PROGRESO, 1, 33)
                ));

        MissionResponse mission = findByName(
                evaluator.evaluate(activity(1, 0.0, 0.0, Map.of(), 1), TENANT_ID),
                "Explorador Verde");
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.EN_PROGRESO);
        assertThat(mission.getProgressPercent()).isEqualTo(33);
    }

    @Test
    void greenPointMission_threeVisits_isCompleted() {
        when(userMissionRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(
                        userMission(exploradorVerde, MissionStatus.COMPLETADA, 3, 100)
                ));

        MissionResponse mission = findByName(
                evaluator.evaluate(activity(3, 0.0, 0.0, Map.of(), 3), TENANT_ID),
                "Explorador Verde");
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.COMPLETADA);
        assertThat(mission.getProgressPercent()).isEqualTo(100);
    }

    // ── MATERIALS_VARIETY mission (target = 3 types) ──────────────────────────

    @Test
    void materialsMission_twoTypes_isInProgress() {
        when(userMissionRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(
                        userMission(diversidadMaterial, MissionStatus.EN_PROGRESO, 2, 67)
                ));

        Map<String, Double> twoMaterials = Map.of("Plástico PET", 5.0, "Papel y Cartón", 5.0);
        MissionResponse mission = findByName(
                evaluator.evaluate(activity(2, 10.0, 0.0, twoMaterials, 1), TENANT_ID),
                "Diversidad Material");
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.EN_PROGRESO);
        assertThat(mission.getProgressPercent()).isEqualTo(67);
    }

    @Test
    void materialsMission_threeTypes_isCompleted() {
        when(userMissionRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(
                        userMission(diversidadMaterial, MissionStatus.COMPLETADA, 3, 100)
                ));

        Map<String, Double> threeMaterials = Map.of(
                "Plástico PET", 5.0, "Papel y Cartón", 5.0, "Vidrio", 3.0);
        MissionResponse mission = findByName(
                evaluator.evaluate(activity(3, 13.0, 0.0, threeMaterials, 2), TENANT_ID),
                "Diversidad Material");
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.COMPLETADA);
        assertThat(mission.getProgressPercent()).isEqualTo(100);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Mission mission(String name, String targetType, double targetValue, int rewardPoints) {
        Mission m = new Mission(name, "descripción", targetType, targetValue, rewardPoints, null);
        m.setTenantId(null);
        return m;
    }

    private com.ecocircular.ecocircular.gamification.domain.UserMission userMission(
            Mission mission, MissionStatus status, double currentValue, int progressPercent) {
        com.ecocircular.ecocircular.gamification.domain.UserMission um =
                new com.ecocircular.ecocircular.gamification.domain.UserMission(
                        USER_ID, mission, status, currentValue, progressPercent,
                        LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())
                );
        um.setTenantId(TENANT_ID);
        return um;
    }

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

    private MissionResponse findByName(List<MissionResponse> missions, String name) {
        return missions.stream()
                .filter(m -> m.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Mission not found: " + name));
    }
}