package com.ecocircular.ecocircular.gamification.application;

import com.ecocircular.ecocircular.common.audit.AuditService;
import com.ecocircular.ecocircular.gamification.api.dto.BadgeResponse;
import com.ecocircular.ecocircular.gamification.application.port.UserRecyclingActivity;
import com.ecocircular.ecocircular.gamification.domain.Badge;
import com.ecocircular.ecocircular.gamification.infrastructure.persistence.BadgeRepository;
import com.ecocircular.ecocircular.gamification.infrastructure.persistence.UserBadgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BadgeEvaluatorTest {

    private static final UUID TENANT_ID = UUID.fromString("a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1");
    private static final UUID USER_ID   = UUID.fromString("b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1");

    @Mock private BadgeRepository     badgeRepository;
    @Mock private UserBadgeRepository userBadgeRepository;
    @Mock private AuditService        auditService;

    @InjectMocks
    private BadgeEvaluator evaluator;

    // Catálogo de badges igual al DataInitializer
    private Badge primerReciclaje;
    private Badge recicladorFrecuente;
    private Badge guardianPlaneta;
    private Badge heroeCo2;
    private Badge exploradorMateriales;
    private Badge nivelOro;

    @BeforeEach
    void setUp() {
        primerReciclaje       = badge("Primer Reciclaje",         "RECYCLE_1");
        recicladorFrecuente   = badge("Reciclador Frecuente",     "RECYCLE_10");
        guardianPlaneta       = badge("Guardián del Planeta",     "EARTH_50KG");
        heroeCo2              = badge("Héroe del CO2",            "CO2_25KG");
        exploradorMateriales  = badge("Explorador de Materiales", "MATERIALS_3");
        nivelOro              = badge("Nivel Oro",                "GOLD_LEVEL");

        // Por defecto el catálogo tiene todos los badges
        when(badgeRepository.findAvailableForTenant(TENANT_ID))
                .thenReturn(List.of(
                        primerReciclaje, recicladorFrecuente, guardianPlaneta,
                        heroeCo2, exploradorMateriales, nivelOro
                ));

        // Por defecto el usuario no tiene ningún badge
        when(userBadgeRepository.existsByUserIdAndBadge_IdAndTenantId(any(), any(), any()))
                .thenReturn(false);

        // findByUserIdAndTenantId devuelve lista vacía por defecto
        when(userBadgeRepository.findByUserIdAndTenantId(any(), any()))
                .thenReturn(List.of());
    }

    @Test
    void evaluate_noActivity_earnsNoBadges() {
        UserRecyclingActivity activity = activity(0, 0.0, 0.0, 0.0, Map.of(), 0, null);
        assertThat(evaluator.evaluate(activity, TENANT_ID)).isEmpty();
    }

    @Test
    void evaluate_oneDelivery_earnsPrimerReciclaje() {
        UserRecyclingActivity activity = activity(1, 1.0, 10.0, 0.5,
                Map.of("Plástico PET", 1.0), 1, LocalDate.now());

        // Simular que después de guardar, el repositorio devuelve el badge
        when(userBadgeRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(new com.ecocircular.ecocircular.gamification.domain.UserBadge(
                        USER_ID, primerReciclaje, java.time.LocalDateTime.now())));

        List<BadgeResponse> badges = evaluator.evaluate(activity, TENANT_ID);
        assertThat(badgeNames(badges)).contains("Primer Reciclaje");
    }

    @Test
    void evaluate_tenDeliveries_earnsRecicladorFrecuente() {
        UserRecyclingActivity activity = activity(10, 30.0, 200.0, 10.0,
                Map.of("Plástico PET", 30.0), 5, LocalDate.now());

        when(userBadgeRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(
                        new com.ecocircular.ecocircular.gamification.domain.UserBadge(USER_ID, primerReciclaje,     java.time.LocalDateTime.now()),
                        new com.ecocircular.ecocircular.gamification.domain.UserBadge(USER_ID, recicladorFrecuente, java.time.LocalDateTime.now())
                ));

        assertThat(badgeNames(evaluator.evaluate(activity, TENANT_ID))).contains("Reciclador Frecuente");
    }

    @Test
    void evaluate_nineDeliveries_doesNotEarnRecicladorFrecuente() {
        UserRecyclingActivity activity = activity(9, 20.0, 180.0, 8.0,
                Map.of("Papel y Cartón", 20.0), 3, LocalDate.now());
        assertThat(badgeNames(evaluator.evaluate(activity, TENANT_ID))).doesNotContain("Reciclador Frecuente");
    }

    @Test
    void evaluate_fiftyKgRecycled_earnsGuardianPlaneta() {
        UserRecyclingActivity activity = activity(5, 50.0, 300.0, 20.0,
                Map.of("Vidrio", 50.0), 3, LocalDate.now());

        when(userBadgeRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(
                        new com.ecocircular.ecocircular.gamification.domain.UserBadge(USER_ID, guardianPlaneta, java.time.LocalDateTime.now())
                ));

        assertThat(badgeNames(evaluator.evaluate(activity, TENANT_ID))).contains("Guardián del Planeta");
    }

    @Test
    void evaluate_twentyFiveCo2Avoided_earnsHeroeCO2() {
        UserRecyclingActivity activity = activity(5, 40.0, 300.0, 25.0,
                Map.of("Metal (Aluminio)", 40.0), 2, LocalDate.now());

        when(userBadgeRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(
                        new com.ecocircular.ecocircular.gamification.domain.UserBadge(USER_ID, heroeCo2, java.time.LocalDateTime.now())
                ));

        assertThat(badgeNames(evaluator.evaluate(activity, TENANT_ID))).contains("Héroe del CO2");
    }

    @Test
    void evaluate_threeMaterialTypes_earnsExploradorMateriales() {
        Map<String, Double> materials = Map.of("Plástico PET", 5.0, "Papel y Cartón", 5.0, "Vidrio", 5.0);
        UserRecyclingActivity activity = activity(3, 15.0, 100.0, 5.0, materials, 2, LocalDate.now());

        when(userBadgeRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(
                        new com.ecocircular.ecocircular.gamification.domain.UserBadge(USER_ID, exploradorMateriales, java.time.LocalDateTime.now())
                ));

        assertThat(badgeNames(evaluator.evaluate(activity, TENANT_ID))).contains("Explorador de Materiales");
    }

    @Test
    void evaluate_twoMaterialTypes_doesNotEarnExploradorMateriales() {
        Map<String, Double> materials = Map.of("Plástico PET", 10.0, "Papel y Cartón", 5.0);
        UserRecyclingActivity activity = activity(3, 15.0, 100.0, 5.0, materials, 2, LocalDate.now());
        assertThat(badgeNames(evaluator.evaluate(activity, TENANT_ID))).doesNotContain("Explorador de Materiales");
    }

    @Test
    void evaluate_fiveHundredPoints_earnsNivelOro() {
        UserRecyclingActivity activity = activity(8, 60.0, 500.0, 22.0,
                Map.of("Plástico PET", 60.0), 4, LocalDate.now());

        when(userBadgeRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(
                        new com.ecocircular.ecocircular.gamification.domain.UserBadge(USER_ID, nivelOro, java.time.LocalDateTime.now())
                ));

        assertThat(badgeNames(evaluator.evaluate(activity, TENANT_ID))).contains("Nivel Oro");
    }

    @Test
    void evaluate_highActivity_earnsAllBadges() {
        Map<String, Double> materials = Map.of("Plástico PET", 30.0, "Papel y Cartón", 20.0, "Vidrio", 15.0);
        UserRecyclingActivity activity = activity(15, 65.0, 600.0, 30.0, materials, 6, LocalDate.now());

        when(userBadgeRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(
                        new com.ecocircular.ecocircular.gamification.domain.UserBadge(USER_ID, primerReciclaje,      java.time.LocalDateTime.now()),
                        new com.ecocircular.ecocircular.gamification.domain.UserBadge(USER_ID, recicladorFrecuente,  java.time.LocalDateTime.now()),
                        new com.ecocircular.ecocircular.gamification.domain.UserBadge(USER_ID, guardianPlaneta,      java.time.LocalDateTime.now()),
                        new com.ecocircular.ecocircular.gamification.domain.UserBadge(USER_ID, heroeCo2,             java.time.LocalDateTime.now()),
                        new com.ecocircular.ecocircular.gamification.domain.UserBadge(USER_ID, exploradorMateriales, java.time.LocalDateTime.now()),
                        new com.ecocircular.ecocircular.gamification.domain.UserBadge(USER_ID, nivelOro,             java.time.LocalDateTime.now())
                ));

        List<String> names = badgeNames(evaluator.evaluate(activity, TENANT_ID));
        assertThat(names).containsExactlyInAnyOrder(
                "Primer Reciclaje", "Reciclador Frecuente", "Guardián del Planeta",
                "Héroe del CO2", "Explorador de Materiales", "Nivel Oro"
        );
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Badge badge(String name, String iconCode) {
        Badge b = new Badge(name, "descripción", iconCode);
        b.setTenantId(null);
        return b;
    }

    private UserRecyclingActivity activity(int deliveries, double kg, double points, double co2,
                                           Map<String, Double> materials, int gpVisits, LocalDate lastDate) {
        return new UserRecyclingActivity(
                USER_ID, "Test", "CIUDADANO", TENANT_ID,
                deliveries, kg, points, co2,
                materials, gpVisits, lastDate
        );
    }

    private List<String> badgeNames(List<BadgeResponse> badges) {
        return badges.stream().map(BadgeResponse::getName).toList();
    }
}