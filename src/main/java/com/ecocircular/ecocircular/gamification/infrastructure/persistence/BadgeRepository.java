package com.ecocircular.ecocircular.gamification.infrastructure.persistence;

import com.ecocircular.ecocircular.gamification.domain.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BadgeRepository extends JpaRepository<Badge, UUID> {

    /** Badges activos del tenant + badges globales (tenant_id IS NULL) */
    @Query("SELECT b FROM Badge b WHERE (b.tenantId = :tenantId OR b.tenantId IS NULL) AND b.active = true")
    List<Badge> findAvailableForTenant(@Param("tenantId") UUID tenantId);
}
