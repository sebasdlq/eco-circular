package com.ecocircular.ecocircular.recyclingops.dto;

import com.ecocircular.ecocircular.recyclingops.domain.DeliveryStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Getter @Setter
public class DeliveryResponse {

    private UUID id;
    private UUID userId;
    private String userDisplayName;
    private UUID greenPointId;
    private String greenPointName;
    private DeliveryStatus status;
    private List<DeliveryDetailResponse> details;
    private ZonedDateTime deliveredAt;
    private ZonedDateTime createdAt;   // de BaseEntity
}