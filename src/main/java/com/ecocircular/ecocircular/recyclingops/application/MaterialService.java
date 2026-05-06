package com.ecocircular.ecocircular.recyclingops.application;

import com.ecocircular.ecocircular.common.base.TenantContext;
import com.ecocircular.ecocircular.recyclingops.domain.MaterialCatalog;
import com.ecocircular.ecocircular.recyclingops.infrastructure.persistence.MaterialCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaterialService {
    private final MaterialCatalogRepository repo;

    public List<MaterialCatalog> getMaterialsForCurrentTenant() {
        UUID tenantId = TenantContext.getTenantId();
        return repo.findAvailableForTenant(tenantId);
    }

    @Transactional
    public MaterialCatalog createMaterial(MaterialCatalog material) {
        // Si se envía tenantId null, se marca como global
        // El prePersist de BaseEntity asignaría tenantId del contexto,
        // pero para global necesitamos forzar null si se desea.
        // Asumimos que si el usuario es admin, puede crear global.
        // Por simplicidad, aquí usamos el contexto, pero permitimos setear manual.
        if (material.getTenantId() == null) {
            material.setTenantId(TenantContext.getTenantId());
        }
        return repo.save(material);
    }

    @Transactional
    public MaterialCatalog updateMaterial(UUID id, MaterialCatalog updated) {
        MaterialCatalog existing = repo.findById(id).orElseThrow();
        existing.setName(updated.getName());
        existing.setCategory(updated.getCategory());
        existing.setUnit(updated.getUnit());
        existing.setPointsPerUnit(updated.getPointsPerUnit());
        existing.setCo2Factor(updated.getCo2Factor());
        existing.setEquivalents(updated.getEquivalents());
        existing.setActive(updated.isActive());
        return repo.save(existing);
    }

    @Transactional
    public void deactivateMaterial(UUID id) {
        MaterialCatalog m = repo.findById(id).orElseThrow();
        m.setActive(false);
        repo.save(m);
    }
}