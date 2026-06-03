package com.ecocircular.ecocircular.gamification.infrastructure.adapter;

import com.ecocircular.ecocircular.gamification.application.port.GamificationActivityPort;
import com.ecocircular.ecocircular.gamification.application.port.UserRecyclingActivity;
import com.ecocircular.ecocircular.iam.domain.UserTenantRole;
import com.ecocircular.ecocircular.iam.infrastructure.persistence.UserRepository;
import com.ecocircular.ecocircular.iam.infrastructure.persistence.UserTenantRoleRepository;
import com.ecocircular.ecocircular.recyclingops.domain.Delivery;
import com.ecocircular.ecocircular.recyclingops.domain.DeliveryDetail;
import com.ecocircular.ecocircular.recyclingops.domain.DeliveryStatus;
import com.ecocircular.ecocircular.recyclingops.infrastructure.persistence.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DeliveryActivityAdapter implements GamificationActivityPort {

    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;
    private final UserTenantRoleRepository userTenantRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserRecyclingActivity getUserRecyclingActivity(UUID tenantId, UUID userId) {
        List<Delivery> deliveries = deliveryRepository
                .findByUser_IdAndGreenPoint_TenantIdAndStatusNot(userId, tenantId, DeliveryStatus.DRAFT);

        String displayName = deliveries.isEmpty()
                ? userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId))
                        .getDisplayName()
                : deliveries.get(0).getUser().getDisplayName();

        String role = userTenantRoleRepository.findByUserId(userId).stream()
                .filter(utr -> utr.getTenant() != null && tenantId.equals(utr.getTenant().getId()))
                .map(UserTenantRole::getRole)
                .findFirst()
                .orElse("MEMBER");

        return buildActivity(tenantId, userId, displayName, role, deliveries);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRecyclingActivity> getAllUsersRecyclingActivity(UUID tenantId) {
        List<Delivery> allDeliveries = deliveryRepository.findByGreenPoint_TenantId(tenantId)
                .stream()
                .filter(d -> d.getStatus() != DeliveryStatus.DRAFT)
                .collect(Collectors.toList());

        Map<UUID, List<Delivery>> byUser = allDeliveries.stream()
                .collect(Collectors.groupingBy(d -> d.getUser().getId()));

        Map<UUID, String> roleByUserId = userTenantRoleRepository.findByTenantIdWithUser(tenantId).stream()
                .collect(Collectors.toMap(
                        utr -> utr.getUser().getId(),
                        UserTenantRole::getRole,
                        (first, second) -> first
                ));

        return byUser.entrySet().stream()
                .map(entry -> {
                    UUID uid = entry.getKey();
                    List<Delivery> userDeliveries = entry.getValue();
                    String displayName = userDeliveries.get(0).getUser().getDisplayName();
                    String role = roleByUserId.getOrDefault(uid, "MEMBER");
                    return buildActivity(tenantId, uid, displayName, role, userDeliveries);
                })
                .collect(Collectors.toList());
    }

    private UserRecyclingActivity buildActivity(UUID tenantId, UUID userId,
                                                String displayName, String role,
                                                List<Delivery> deliveries) {
        double totalKgRecycled = 0;
        double totalPoints = 0;
        double totalCo2Avoided = 0;
        Map<String, Double> materialsRecycled = new HashMap<>();

        for (Delivery delivery : deliveries) {
            for (DeliveryDetail detail : delivery.getDetails()) {
                totalKgRecycled += detail.getQuantity();
                totalPoints += detail.getPointsEarned();
                if (detail.getCo2Estimated() != null) {
                    totalCo2Avoided += detail.getCo2Estimated().doubleValue();
                }
                materialsRecycled.merge(
                        detail.getMaterial().getName(),
                        detail.getQuantity(),
                        Double::sum
                );
            }
        }

        int greenPointVisits = (int) deliveries.stream()
                .map(d -> d.getGreenPoint().getId())
                .distinct()
                .count();

        LocalDate lastDeliveryDate = deliveries.stream()
                .map(d -> d.getDeliveredAt() != null ? d.getDeliveredAt().toLocalDate() : null)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);

        return new UserRecyclingActivity(
                userId, displayName, role, tenantId,
                deliveries.size(),
                totalKgRecycled,
                totalPoints,
                totalCo2Avoided,
                Collections.unmodifiableMap(materialsRecycled),
                greenPointVisits,
                lastDeliveryDate
        );
    }
}
