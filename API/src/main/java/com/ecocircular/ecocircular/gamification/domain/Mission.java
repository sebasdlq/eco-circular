package com.ecocircular.ecocircular.gamification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class Mission {
    private final UUID id;
    private final String name;
    private final String description;
    // DELIVERIES | KG_RECYCLED | GREEN_POINT_VISITS | MATERIALS_VARIETY
    private final String targetType;
    private final double targetValue;
    private final int rewardPoints;
}
