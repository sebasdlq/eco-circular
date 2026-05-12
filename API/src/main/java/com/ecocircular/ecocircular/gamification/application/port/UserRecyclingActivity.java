package com.ecocircular.ecocircular.gamification.application.port;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Data snapshot provided to Dev 5 by the GamificationActivityPort.
 * Represents what a single user has recycled.
 *
 * OWNERSHIP NOTE:
 *  - Delivery creation, validation, QR flow, base points and CO2 calculation → Dev 2
 *  - This object is populated by the port adapter, not by Dev 5
 *  - Replace MockGamificationActivityAdapter with a real adapter once Dev 2 exposes delivery data
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
