package com.ecocircular.ecocircular.recyclingops.infrastructure.persistence;

import com.ecocircular.ecocircular.recyclingops.domain.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    // Existente — para operador/admin
    List<Delivery> findByGreenPoint_TenantId(UUID tenantId);

    // ✅ Nuevo — historial del ciudadano logueado
    List<Delivery> findByUser_IdOrderByDeliveredAtDesc(UUID userId);

    // ✅ Nuevo — ranking del tenant (suma de puntos por usuario)
    @Query("""
        SELECT d.user.id, d.user.displayName,
               SUM(det.pointsEarned) as totalPoints,
               SUM(det.quantity)     as totalKg
        FROM Delivery d
        JOIN d.details det
        WHERE d.greenPoint.tenantId = :tenantId
          AND d.status <> 'DRAFT'
        GROUP BY d.user.id, d.user.displayName
        ORDER BY totalPoints DESC
    """)
    List<Object[]> findRankingByTenant(@Param("tenantId") UUID tenantId);
}