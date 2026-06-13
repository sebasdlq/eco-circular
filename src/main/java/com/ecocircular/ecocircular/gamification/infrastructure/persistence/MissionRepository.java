package com.ecocircular.ecocircular.gamification.infrastructure.persistence;

import com.ecocircular.ecocircular.gamification.domain.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MissionRepository extends JpaRepository<Mission, UUID> {

    /** Misiones activas del tenant + misiones globales (tenant_id IS NULL) */
    @Query("SELECT m FROM Mission m WHERE (m.tenantId = :tenantId OR m.tenantId IS NULL) AND m.active = true")
    List<Mission> findAvailableForTenant(@Param("tenantId") UUID tenantId);
}
