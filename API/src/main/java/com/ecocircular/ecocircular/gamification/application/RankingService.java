package com.ecocircular.ecocircular.gamification.application;

import com.ecocircular.ecocircular.gamification.application.port.GamificationActivityPort;
import com.ecocircular.ecocircular.gamification.application.port.UserRecyclingActivity;
import com.ecocircular.ecocircular.gamification.domain.RankingEntry;
import com.ecocircular.ecocircular.gamification.domain.RankingScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final GamificationActivityPort activityPort;
    private final LevelCalculator levelCalculator;

    public List<RankingEntry> getRanking(UUID tenantId, RankingScope scope) {
        List<UserRecyclingActivity> all = activityPort.getAllUsersRecyclingActivity(tenantId);

        List<UserRecyclingActivity> filtered = switch (scope) {
            case SCHOOL -> all.stream()
                    .filter(a -> "ESTUDIANTE".equals(a.getRole()) || "DOCENTE".equals(a.getRole()))
                    .toList();
            case ZONE -> all.stream()
                    .filter(a -> a.getGreenPointVisits() > 0)
                    .toList();
            case GLOBAL -> all;
        };

        List<UserRecyclingActivity> sorted = filtered.stream()
                .sorted(Comparator.comparingDouble(UserRecyclingActivity::getTotalPoints).reversed())
                .toList();

        List<RankingEntry> entries = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            UserRecyclingActivity a = sorted.get(i);
            entries.add(new RankingEntry(
                    i + 1,
                    a.getUserId(),
                    a.getDisplayName(),
                    a.getTotalPoints(),
                    levelCalculator.calculate(a.getTotalPoints()),
                    scope
            ));
        }
        return entries;
    }
}
