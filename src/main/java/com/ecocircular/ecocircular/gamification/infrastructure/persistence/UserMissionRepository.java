package com.ecocircular.ecocircular.gamification.infrastructure.persistence;

import com.ecocircular.ecocircular.gamification.domain.UserMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserMissionRepository extends JpaRepository<UserMission, UUID> {

    List<UserMission> findByUserIdAndTenantId(UUID userId, UUID tenantId);

    Optional<UserMission> findByUserIdAndMission_IdAndTenantId(UUID userId, UUID missionId, UUID tenantId);
}
