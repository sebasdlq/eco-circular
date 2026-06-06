package com.ecocircular.ecocircular.gamification.api.dto;

import com.ecocircular.ecocircular.gamification.domain.UserBadge;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class BadgeResponse {
    private final UUID badgeId;
    private final String name;
    private final String description;
    private final String iconCode;
    private final LocalDateTime earnedAt;

    public static BadgeResponse from(UserBadge ub) {
        return new BadgeResponse(
                ub.getBadge().getId(),
                ub.getBadge().getName(),
                ub.getBadge().getDescription(),
                ub.getBadge().getIconCode(),
                ub.getEarnedAt()
        );
    }
}
