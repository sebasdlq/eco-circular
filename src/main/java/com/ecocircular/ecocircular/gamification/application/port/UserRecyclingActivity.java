package com.ecocircular.ecocircular.gamification.application.port;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Data snapshot populated by DeliveryActivityAdapter from Delivery and DeliveryDetail entities.
 * All aggregates (points, kg, CO2) are computed from non-DRAFT deliveries within the tenant.
 */
@Getter
@AllArgsConstructor
public class UserRecyclingActivity {
    private final UUID userId;
    private final String displayName;
    private final String role;
    private final UUID tenantId;
    private final int totalDeliveries;
    private final double totalKgRecycled;
    private final double totalPoints;
    private final double totalCo2AvoidedKg;
    /** Material name → kg recycled */
    private final Map<String, Double> materialsRecycled;
    private final int greenPointVisits;
    /** Null if the user has never made a delivery */
    private final LocalDate lastDeliveryDate;
}
