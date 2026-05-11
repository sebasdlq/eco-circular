package com.ecocircular.ecocircular.recyclingops.infrastructure.persistence;

import com.ecocircular.ecocircular.recyclingops.domain.MaterialCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface MaterialCatalogRepository extends JpaRepository<MaterialCatalog, UUID> {
    // Obtener materiales del tenant y globales (tenant_id IS NULL)
    @Query("SELECT m FROM MaterialCatalog m WHERE m.tenantId = :tenantId OR m.tenantId IS NULL")
    List<MaterialCatalog> findAvailableForTenant(@Param("tenantId") UUID tenantId);
}