package com.ecocircular.ecocircular.gamification.api.dto;

import com.ecocircular.ecocircular.gamification.domain.Recommendation;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecommendationResponse {
    private final String type;
    private final String message;
    private final int priority;

    public static RecommendationResponse from(Recommendation r) {
        return new RecommendationResponse(r.getType(), r.getMessage(), r.getPriority());
    }
}
