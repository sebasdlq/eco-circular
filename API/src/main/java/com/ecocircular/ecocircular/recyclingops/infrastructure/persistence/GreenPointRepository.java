package com.ecocircular.ecocircular.recyclingops.infrastructure.persistence;

import com.ecocircular.ecocircular.recyclingops.domain.GreenPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface GreenPointRepository extends JpaRepository<GreenPoint, UUID> {
    List<GreenPoint> findByTenantId(UUID tenantId);
}
