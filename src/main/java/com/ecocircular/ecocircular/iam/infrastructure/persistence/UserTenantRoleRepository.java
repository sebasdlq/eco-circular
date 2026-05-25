package com.ecocircular.ecocircular.iam.infrastructure.persistence;

import com.ecocircular.ecocircular.iam.domain.Tenant;
import com.ecocircular.ecocircular.iam.domain.User;
import com.ecocircular.ecocircular.iam.domain.UserTenantRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserTenantRoleRepository extends JpaRepository<UserTenantRole, UUID> {

    List<UserTenantRole> findByUserId(UUID userId);

    List<UserTenantRole> findByTenantId(UUID tenantId);

    boolean existsByUserAndTenantAndRole(User user, Tenant tenant, String role);
}