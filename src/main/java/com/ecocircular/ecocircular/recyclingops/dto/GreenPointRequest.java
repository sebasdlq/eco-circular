package com.ecocircular.ecocircular.recyclingops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GreenPointRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotNull(message = "La latitud es obligatoria")
    private Double locationLat;

    @NotNull(message = "La longitud es obligatoria")
    private Double locationLng;

    /** JSON con horarios: {"lun":"08:00-18:00", "sab":"09:00-14:00"} */
    private String schedule;

    /** JSON con capacidad: {"plastico":500, "vidrio":200} */
    private String capacity;

    /** IDs de materiales del catálogo que acepta este punto */
    private List<UUID> acceptedMaterialIds;
}