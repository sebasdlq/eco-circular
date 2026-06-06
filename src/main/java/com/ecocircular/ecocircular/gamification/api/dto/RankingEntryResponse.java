package com.ecocircular.ecocircular.gamification.api.dto;

import com.ecocircular.ecocircular.gamification.domain.RankingEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class RankingEntryResponse {
    private final int rank;
    private final UUID userId;
    private final String displayName;
    private final double score;
    private final String level;

    public static RankingEntryResponse from(RankingEntry e) {
        return new RankingEntryResponse(
                e.getRank(), e.getUserId(), e.getDisplayName(),
                e.getScore(), e.getLevel().name()
        );
    }
}
