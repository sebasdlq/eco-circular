package com.ecocircular.ecocircular.iam.infrastructure.persistence;

import com.ecocircular.ecocircular.iam.domain.Tenant;
import com.ecocircular.ecocircular.iam.domain.User;
import com.ecocircular.ecocircular.iam.domain.UserTenantRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTenantRoleRepository extends JpaRepository<UserTenantRole, UUID> {

    List<UserTenantRole> findByUserId(UUID userId);

    List<UserTenantRole> findByTenantId(UUID tenantId);

    boolean existsByUserAndTenantAndRole(User user, Tenant tenant, String role);

    // Eager-fetches user to avoid N+1 when building gamification activity for all tenant users
    @Query("SELECT utr FROM UserTenantRole utr JOIN FETCH utr.user WHERE utr.tenant.id = :tenantId")
    List<UserTenantRole> findByTenantIdWithUser(@Param("tenantId") UUID tenantId);

    Optional<UserTenantRole> findByUserIdAndTenantId(UUID id, UUID tenantId);
}