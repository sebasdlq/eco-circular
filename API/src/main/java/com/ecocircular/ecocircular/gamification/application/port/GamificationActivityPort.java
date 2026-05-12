package com.ecocircular.ecocircular.gamification.application.port;

import java.util.List;
import java.util.UUID;

/**
 * Output port — boundary between Dev 5 (gamification) and Dev 2 (deliveries).
 *
 * OWNERSHIP NOTE:
 *  - This interface is owned by Dev 5 and must not be changed by Dev 2.
 *  - Dev 2 must implement a real adapter that satisfies this contract using
 *    Entrega and EntregaDetalle once those entities are available.
 *  - Until then, MockGamificationActivityAdapter provides simulated data.
 */
public interface GamificationActivityPort {

    UserRecyclingActivity getUserRecyclingActivity(UUID tenantId, UUID userId);

    List<UserRecyclingActivity> getAllUsersRecyclingActivity(UUID tenantId);
}
