package com.ecocircular.ecocircular.gamification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserBadge {
    private final UUID userId;
    private final Badge badge;
    private final LocalDateTime earnedAt;
}
