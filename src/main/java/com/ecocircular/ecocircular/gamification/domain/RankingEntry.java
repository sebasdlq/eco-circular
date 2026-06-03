package com.ecocircular.ecocircular.gamification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class RankingEntry {
    private final int rank;
    private final UUID userId;
    private final String displayName;
    private final double score;
    private final UserLevel level;
    private final RankingScope scope;
}
