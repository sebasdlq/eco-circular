package com.ecocircular.ecocircular.recyclingops.application;

import com.ecocircular.ecocircular.common.base.TenantContext;
import com.ecocircular.ecocircular.iam.infrastructure.persistence.UserRepository;
import com.ecocircular.ecocircular.recyclingops.domain.Delivery;
import com.ecocircular.ecocircular.recyclingops.dto.*;
import com.ecocircular.ecocircular.recyclingops.infrastructure.persistence.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitizenService {

    private final DeliveryRepository deliveryRepository;
    private final UserRepository     userRepository;
    private final DeliveryService    deliveryService;

    // ── Historial del ciudadano logueado ─────────────────────────────────────
    public List<DeliveryResponse> getMyHistory(UUID userId) {
        return deliveryRepository
                .findByUser_IdOrderByDeliveredAtDesc(userId)
                .stream()
                .map(deliveryService::toPublicResponse)
                .collect(Collectors.toList());
    }

    // ── Impacto personal ─────────────────────────────────────────────────────
    public CitizenImpactResponse getMyImpact(UUID userId) {
        List<Delivery> deliveries = deliveryRepository
                .findByUser_IdOrderByDeliveredAtDesc(userId);

        double totalKg     = 0;
        double totalCo2    = 0;
        int    totalPoints = 0;

        for (Delivery d : deliveries) {
            for (var det : d.getDetails()) {
                totalKg     += det.getQuantity();
                totalCo2    += det.getCo2Estimated() != null
                        ? det.getCo2Estimated().doubleValue() : 0;
                totalPoints += det.getPointsEarned();
            }
        }

        return new CitizenImpactResponse(
                Math.round(totalKg     * 10.0) / 10.0,
                Math.round(totalCo2    * 10.0) / 10.0,
                totalPoints,
                deliveries.size()
        );
    }

    // ── Ranking del tenant ───────────────────────────────────────────────────
    public List<CitizenRankingEntry> getRanking() {
        UUID tenantId = TenantContext.getTenantId();
        List<Object[]> rows = deliveryRepository.findRankingByTenant(tenantId);

        List<CitizenRankingEntry> ranking = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Object[] row = rows.get(i);
            ranking.add(new CitizenRankingEntry(
                    i + 1,
                    (UUID)   row[0],
                    (String) row[1],
                    ((Number) row[2]).intValue(),
                    ((Number) row[3]).doubleValue()
            ));
        }
        return ranking;
    }
}