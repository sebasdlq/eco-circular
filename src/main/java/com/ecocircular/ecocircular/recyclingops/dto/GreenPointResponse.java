package com.ecocircular.ecocircular.recyclingops.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GreenPointResponse {
    private UUID id;
    private UUID tenantId;
    private String name;
    private Double locationLat;
    private Double locationLng;
    private String schedule;
    private String capacity;
    private String status;
    /** Materiales aceptados con su info completa (para el mapa y la app móvil) */
    private List<MaterialResponse> acceptedMaterials;
}
