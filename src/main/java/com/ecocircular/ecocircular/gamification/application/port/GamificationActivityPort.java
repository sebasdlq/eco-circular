package com.ecocircular.ecocircular.gamification.application.port;

import java.util.List;
import java.util.UUID;

/**
 * Output port — boundary between Dev 5 (gamification) and Dev 2 (deliveries).
 *
 * OWNERSHIP NOTE:
 *  - This interface is owned by Dev 5 and must not be changed by Dev 2.
 *  - Implemented by DeliveryActivityAdapter, which reads from Delivery and DeliveryDetail.
 */
public interface GamificationActivityPort {

    UserRecyclingActivity getUserRecyclingActivity(UUID tenantId, UUID userId);

    List<UserRecyclingActivity> getAllUsersRecyclingActivity(UUID tenantId);
}
