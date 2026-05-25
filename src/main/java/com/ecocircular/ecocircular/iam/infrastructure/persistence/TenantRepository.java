package com.ecocircular.ecocircular.iam.infrastructure.persistence;

import com.ecocircular.ecocircular.iam.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByNameAndType(String name, String type);
}
