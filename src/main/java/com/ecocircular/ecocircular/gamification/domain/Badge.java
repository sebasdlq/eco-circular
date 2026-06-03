package com.ecocircular.ecocircular.gamification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class Badge {
    private final UUID id;
    private final String name;
    private final String description;
    private final String iconCode;
}
