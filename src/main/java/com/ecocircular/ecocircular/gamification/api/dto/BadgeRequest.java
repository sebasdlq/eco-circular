package com.ecocircular.ecocircular.gamification.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor
public class BadgeRequest {

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String iconCode;

    private UUID tenantId; // null = global
}