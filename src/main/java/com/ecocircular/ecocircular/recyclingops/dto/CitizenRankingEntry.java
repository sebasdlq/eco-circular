package com.ecocircular.ecocircular.recyclingops.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CitizenRankingEntry {
    private int    position;
    private UUID   userId;
    private String displayName;
    private int    totalPoints;
    private double totalKg;
}