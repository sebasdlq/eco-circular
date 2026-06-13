package com.ecocircular.ecocircular.gamification.infrastructure.persistence;

import com.ecocircular.ecocircular.gamification.domain.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserBadgeRepository extends JpaRepository<UserBadge, UUID> {

    List<UserBadge> findByUserIdAndTenantId(UUID userId, UUID tenantId);

    boolean existsByUserIdAndBadge_IdAndTenantId(UUID userId, UUID badgeId, UUID tenantId);
}
