package com.ecocircular.ecocircular.recyclingops.application;

import com.ecocircular.ecocircular.common.base.TenantContext;
import com.ecocircular.ecocircular.recyclingops.domain.MaterialCatalog;
import com.ecocircular.ecocircular.recyclingops.dto.MaterialRequest;
import com.ecocircular.ecocircular.recyclingops.dto.MaterialResponse;
import com.ecocircular.ecocircular.recyclingops.infrastructure.persistence.MaterialCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialCatalogRepository repo;

    @Transactional(readOnly = true)
    public List<MaterialResponse> getMaterialsForCurrentTenant() {
        UUID tenantId = TenantContext.getTenantId();
        return repo.findAvailableForTenant(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MaterialResponse createMaterial(MaterialRequest request) {
        MaterialCatalog material = new MaterialCatalog();
        // Si no envían tenantId se asigna el del contexto; null = global (solo admins)
        material.setTenantId(request.getTenantId() != null
                ? request.getTenantId()
                : TenantContext.getTenantId());
        applyRequest(material, request);
        return toResponse(repo.save(material));
    }

    @Transactional
    public MaterialResponse updateMaterial(UUID id, MaterialRequest request) {
        MaterialCatalog existing = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Material no encontrado: " + id));
        applyRequest(existing, request);
        return toResponse(repo.save(existing));
    }

    @Transactional
    public void deactivateMaterial(UUID id) {
        MaterialCatalog m = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Material no encontrado: " + id));
        m.setActive(false);
        repo.save(m);
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private void applyRequest(MaterialCatalog m, MaterialRequest req) {
        m.setName(req.getName());
        m.setCategory(req.getCategory());
        m.setUnit(req.getUnit());
        m.setPointsPerUnit(req.getPointsPerUnit());
        m.setCo2Factor(req.getCo2Factor());
        m.setEquivalents(req.getEquivalents());
        m.setActive(true);
        if (req.getEffectiveFrom() != null) m.setEffectiveFrom(req.getEffectiveFrom());
    }

    public MaterialResponse toResponse(MaterialCatalog m) {
        MaterialResponse r = new MaterialResponse();
        r.setId(m.getId());
        r.setTenantId(m.getTenantId());
        r.setName(m.getName());
        r.setCategory(m.getCategory());
        r.setUnit(m.getUnit());
        r.setPointsPerUnit(m.getPointsPerUnit());
        r.setCo2Factor(m.getCo2Factor());
        r.setEquivalents(m.getEquivalents());
        r.setActive(m.isActive());
        r.setEffectiveFrom(m.getEffectiveFrom());
        return r;
    }
}