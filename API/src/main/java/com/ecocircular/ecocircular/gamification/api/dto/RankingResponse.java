package com.ecocircular.ecocircular.gamification.api.dto;

import com.ecocircular.ecocircular.gamification.domain.RankingEntry;
import com.ecocircular.ecocircular.gamification.domain.RankingScope;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class RankingResponse {
    private final String scope;
    private final String period;
    private final int totalParticipants;
    private final List<RankingEntryResponse> entries;

    public static RankingResponse from(RankingScope scope, List<RankingEntry> entries) {
        return new RankingResponse(
                scope.name(),
                LocalDate.now().toString(),
                entries.size(),
                entries.stream().map(RankingEntryResponse::from).toList()
        );
    }
}
