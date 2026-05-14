package com.ecocircular.ecocircular.recyclingops.infrastructure.persistence;

import com.ecocircular.ecocircular.recyclingops.domain.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
}
