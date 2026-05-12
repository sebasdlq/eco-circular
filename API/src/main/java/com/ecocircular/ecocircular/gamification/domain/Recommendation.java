package com.ecocircular.ecocircular.gamification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Recommendation {
    // FREQUENCY_TIP | MATERIAL_TIP | LEVEL_TIP | MISSION_TIP | GREENPOINT_TIP
    private final String type;
    private final String message;
    private final int priority; // 1 = high, 2 = medium, 3 = low
}
