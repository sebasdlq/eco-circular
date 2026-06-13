package com.ecocircular.ecocircular.gamification.api;

import com.ecocircular.ecocircular.common.base.TenantContext;
import com.ecocircular.ecocircular.common.exception.GlobalExceptionHandler;
import com.ecocircular.ecocircular.gamification.api.dto.BadgeResponse;
import com.ecocircular.ecocircular.gamification.api.dto.MissionResponse;
import com.ecocircular.ecocircular.gamification.application.GamificationService;
import com.ecocircular.ecocircular.gamification.domain.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GamificationControllerTest {

    @Mock GamificationService gamificationService;

    MockMvc mockMvc;

    private static final UUID TENANT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID USER_ID   = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new GamificationController(gamificationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        TenantContext.setTenantId(TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // ── GET /api/gamification/users/{userId} ────────────────────────────────

    @Test
    void getUserSummary_returns200WithCorrectFields() throws Exception {
        UserGamificationSummary summary = new UserGamificationSummary(
                USER_ID, "Test User", UserLevel.BRONCE, UserLevel.BRONCE.getDisplayName(),
                50.0, 2.0, 5.0, 1, 1, 3, 50);

        when(gamificationService.getSummary(TENANT_ID, USER_ID)).thenReturn(summary);

        mockMvc.perform(get("/api/gamification/users/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.displayName").value("Test User"))
                .andExpect(jsonPath("$.level").value("BRONCE"))
                .andExpect(jsonPath("$.totalDeliveries").value(1))
                .andExpect(jsonPath("$.badgesEarned").value(1));
    }

    @Test
    void getUserSummary_userNotFound_returns404() throws Exception {
        when(gamificationService.getSummary(any(), any()))
                .thenThrow(new IllegalArgumentException("Usuario no encontrado: " + USER_ID));

        mockMvc.perform(get("/api/gamification/users/{id}", USER_ID))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/gamification/users/{userId}/badges ─────────────────────────

    @Test
    void getUserBadges_returns200WithList() throws Exception {
        BadgeResponse badgeResponse = new BadgeResponse(
                UUID.randomUUID(),
                "Primer Reciclaje",
                "desc",
                "RECYCLE_1",
                java.time.LocalDateTime.now()
        );

        when(gamificationService.getBadges(TENANT_ID, USER_ID))
                .thenReturn(List.of(badgeResponse));

        mockMvc.perform(get("/api/gamification/users/{id}/badges", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Primer Reciclaje"));
    }

    @Test
    void getUserBadges_noDeliveries_returns200EmptyList() throws Exception {
        when(gamificationService.getBadges(TENANT_ID, USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/gamification/users/{id}/badges", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET /api/gamification/users/{userId}/missions ───────────────────────

    @Test
    void getUserMissions_returns200WithList() throws Exception {
        MissionResponse missionResponse = new MissionResponse(
                UUID.randomUUID(),
                "Primer Paso Verde",
                "Realiza tu primera entrega",
                MissionStatus.EN_PROGRESO,
                50,
                0.5,
                1.0,
                50,
                java.time.LocalDate.now().plusDays(10)
        );

        when(gamificationService.getMissions(TENANT_ID, USER_ID))
                .thenReturn(List.of(missionResponse));

        mockMvc.perform(get("/api/gamification/users/{id}/missions", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Primer Paso Verde"));
    }

    // ── GET /api/gamification/users/{userId}/recommendation ─────────────────

    @Test
    void getUserRecommendations_returns200() throws Exception {
        when(gamificationService.getRecommendations(TENANT_ID, USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/gamification/users/{id}/recommendation", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ── GET /api/gamification/ranking ───────────────────────────────────────

    @Test
    void getRanking_defaultScope_returns200() throws Exception {
        when(gamificationService.getRanking(eq(TENANT_ID), eq(RankingScope.GLOBAL)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/gamification/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("GLOBAL"))
                .andExpect(jsonPath("$.totalParticipants").value(0));
    }

    @Test
    void getRanking_schoolScope_returns200() throws Exception {
        when(gamificationService.getRanking(eq(TENANT_ID), eq(RankingScope.SCHOOL)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/gamification/ranking").param("scope", "SCHOOL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("SCHOOL"));
    }

    @Test
    void getRanking_invalidScope_returns400() throws Exception {
        mockMvc.perform(get("/api/gamification/ranking").param("scope", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRanking_lowercaseScope_isAccepted() throws Exception {
        when(gamificationService.getRanking(eq(TENANT_ID), eq(RankingScope.ZONE)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/gamification/ranking").param("scope", "zone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("ZONE"));
    }

    @Test
    void getRanking_withEntries_includesRankAndLevel() throws Exception {
        RankingEntry entry = new RankingEntry(1, USER_ID, "Test User", 600.0, UserLevel.ORO, RankingScope.GLOBAL);
        when(gamificationService.getRanking(eq(TENANT_ID), eq(RankingScope.GLOBAL)))
                .thenReturn(List.of(entry));

        mockMvc.perform(get("/api/gamification/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalParticipants").value(1))
                .andExpect(jsonPath("$.entries[0].rank").value(1))
                .andExpect(jsonPath("$.entries[0].level").value("ORO"))
                .andExpect(jsonPath("$.entries[0].score").value(600.0));
    }
}
