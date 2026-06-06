package com.ecocircular.ecocircular.gamification.application;

import com.ecocircular.ecocircular.gamification.application.port.GamificationActivityPort;
import com.ecocircular.ecocircular.gamification.application.port.UserRecyclingActivity;
import com.ecocircular.ecocircular.gamification.domain.RankingEntry;
import com.ecocircular.ecocircular.gamification.domain.RankingScope;
import com.ecocircular.ecocircular.gamification.domain.UserLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock GamificationActivityPort activityPort;

    private final LevelCalculator levelCalculator = new LevelCalculator();
    private RankingService rankingService;

    private static final UUID TENANT_ID  = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID USER_1     = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID USER_2     = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID USER_3     = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @BeforeEach
    void setUp() {
        rankingService = new RankingService(activityPort, levelCalculator);
    }

    @Test
    void getRanking_global_returnsAllUsersSortedByPointsDesc() {
        when(activityPort.getAllUsersRecyclingActivity(TENANT_ID)).thenReturn(List.of(
                activity(USER_1, "Ana",   "CIUDADANO", 200.0, 0),
                activity(USER_2, "Juan",  "CIUDADANO", 600.0, 1),
                activity(USER_3, "María", "CIUDADANO", 80.0,  0)
        ));

        List<RankingEntry> ranking = rankingService.getRanking(TENANT_ID, RankingScope.GLOBAL);

        assertThat(ranking).hasSize(3);
        assertThat(ranking.get(0).getUserId()).isEqualTo(USER_2); // 600 pts — first
        assertThat(ranking.get(1).getUserId()).isEqualTo(USER_1); // 200 pts — second
        assertThat(ranking.get(2).getUserId()).isEqualTo(USER_3); // 80 pts — third
    }

    @Test
    void getRanking_global_rankNumbersAreSequential() {
        when(activityPort.getAllUsersRecyclingActivity(TENANT_ID)).thenReturn(List.of(
                activity(USER_1, "Ana",  "CIUDADANO", 300.0, 1),
                activity(USER_2, "Juan", "CIUDADANO", 100.0, 0)
        ));

        List<RankingEntry> ranking = rankingService.getRanking(TENANT_ID, RankingScope.GLOBAL);

        assertThat(ranking.get(0).getRank()).isEqualTo(1);
        assertThat(ranking.get(1).getRank()).isEqualTo(2);
    }

    @Test
    void getRanking_global_levelIsCalculatedCorrectly() {
        when(activityPort.getAllUsersRecyclingActivity(TENANT_ID)).thenReturn(List.of(
                activity(USER_1, "Ana", "CIUDADANO", 600.0, 1) // ORO
        ));

        RankingEntry entry = rankingService.getRanking(TENANT_ID, RankingScope.GLOBAL).get(0);

        assertThat(entry.getLevel()).isEqualTo(UserLevel.ORO);
    }

    @Test
    void getRanking_school_returnsOnlyEstudianteAndDocente() {
        when(activityPort.getAllUsersRecyclingActivity(TENANT_ID)).thenReturn(List.of(
                activity(USER_1, "Estudiante", "ESTUDIANTE",  100.0, 1),
                activity(USER_2, "Docente",    "DOCENTE",     200.0, 2),
                activity(USER_3, "Admin",      "CIUDADANO",   500.0, 3)
        ));

        List<RankingEntry> ranking = rankingService.getRanking(TENANT_ID, RankingScope.SCHOOL);

        assertThat(ranking).hasSize(2);
        assertThat(ranking.stream().map(RankingEntry::getUserId))
                .containsExactlyInAnyOrder(USER_1, USER_2);
    }

    @Test
    void getRanking_school_noSchoolUsers_returnsEmpty() {
        when(activityPort.getAllUsersRecyclingActivity(TENANT_ID)).thenReturn(List.of(
                activity(USER_1, "Admin", "CIUDADANO", 500.0, 2)
        ));

        assertThat(rankingService.getRanking(TENANT_ID, RankingScope.SCHOOL)).isEmpty();
    }

    @Test
    void getRanking_zone_returnsOnlyUsersWithGreenPointVisits() {
        when(activityPort.getAllUsersRecyclingActivity(TENANT_ID)).thenReturn(List.of(
                activity(USER_1, "Con visitas",    "CIUDADANO", 300.0, 2),
                activity(USER_2, "Sin visitas",    "CIUDADANO", 500.0, 0),
                activity(USER_3, "Más visitas",    "CIUDADANO", 100.0, 5)
        ));

        List<RankingEntry> ranking = rankingService.getRanking(TENANT_ID, RankingScope.ZONE);

        assertThat(ranking).hasSize(2);
        assertThat(ranking.stream().map(RankingEntry::getUserId))
                .containsExactlyInAnyOrder(USER_1, USER_3);
    }

    @Test
    void getRanking_zone_sortedByPointsDesc() {
        when(activityPort.getAllUsersRecyclingActivity(TENANT_ID)).thenReturn(List.of(
                activity(USER_1, "Low",  "CIUDADANO", 50.0,  1),
                activity(USER_2, "High", "CIUDADANO", 400.0, 2)
        ));

        List<RankingEntry> ranking = rankingService.getRanking(TENANT_ID, RankingScope.ZONE);

        assertThat(ranking.get(0).getUserId()).isEqualTo(USER_2);
        assertThat(ranking.get(1).getUserId()).isEqualTo(USER_1);
    }

    @Test
    void getRanking_emptyTenant_returnsEmptyForAllScopes() {
        when(activityPort.getAllUsersRecyclingActivity(TENANT_ID)).thenReturn(List.of());

        assertThat(rankingService.getRanking(TENANT_ID, RankingScope.GLOBAL)).isEmpty();
        assertThat(rankingService.getRanking(TENANT_ID, RankingScope.SCHOOL)).isEmpty();
        assertThat(rankingService.getRanking(TENANT_ID, RankingScope.ZONE)).isEmpty();
    }

    // ── helper ──────────────────────────────────────────────────────────────

    private UserRecyclingActivity activity(UUID userId, String name, String role,
                                           double points, int gpVisits) {
        return new UserRecyclingActivity(
                userId, name, role, TENANT_ID,
                1, 5.0, points, 2.0,
                Map.of("Plástico PET", 5.0),
                gpVisits, LocalDate.now()
        );
    }
}
