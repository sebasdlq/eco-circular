package com.ecocircular.ecocircular.recyclingops.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter @Setter
public class DeliveryCreateRequest {

    @NotNull(message = "El userId es obligatorio")
    private UUID userId;

    @NotNull(message = "El greenPointId es obligatorio")
    private UUID greenPointId;

    @NotEmpty(message = "Debe incluir al menos un detalle")
    @Valid
    private List<DeliveryDetailRequest> details;
}