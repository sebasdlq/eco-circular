package com.ecocircular.ecocircular.recyclingops.infrastructure.persistence;

import com.ecocircular.ecocircular.recyclingops.domain.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    List<Delivery> findByGreenPoint_TenantId(UUID tenantId);
    List<Delivery> findByUserId(UUID userId);
}
