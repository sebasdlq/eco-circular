package com.ecocircular.ecocircular.recyclingops.application;

import com.ecocircular.ecocircular.common.base.TenantContext;
import com.ecocircular.ecocircular.recyclingops.domain.GreenPoint;
import com.ecocircular.ecocircular.recyclingops.domain.MaterialCatalog;
import com.ecocircular.ecocircular.recyclingops.dto.GreenPointRequest;
import com.ecocircular.ecocircular.recyclingops.dto.GreenPointResponse;
import com.ecocircular.ecocircular.recyclingops.infrastructure.persistence.GreenPointRepository;
import com.ecocircular.ecocircular.recyclingops.infrastructure.persistence.MaterialCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GreenPointService {

    private final GreenPointRepository repo;
    private final MaterialCatalogRepository materialRepo;
    private final MaterialService materialService;

    @Transactional(readOnly = true)
    public List<GreenPointResponse> getAllForTenant() {
        return repo.findByTenantId(TenantContext.getTenantId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GreenPointResponse getById(UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        GreenPoint gp = repo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Punto verde no encontrado: " + id));
        return toResponse(gp);
    }

    @Transactional
    public GreenPointResponse create(GreenPointRequest request) {
        GreenPoint gp = new GreenPoint();
        gp.setTenantId(TenantContext.getTenantId());
        gp.setStatus("ACTIVE");
        applyRequest(gp, request);
        return toResponse(repo.save(gp));
    }

    @Transactional
    public GreenPointResponse update(UUID id, GreenPointRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        GreenPoint gp = repo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Punto verde no encontrado: " + id));
        applyRequest(gp, request);
        return toResponse(repo.save(gp));
    }

    @Transactional
    public void disable(UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        GreenPoint gp = repo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Punto verde no encontrado: " + id));
        gp.setStatus("INACTIVE");
        repo.save(gp);
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private void applyRequest(GreenPoint gp, GreenPointRequest req) {
        gp.setName(req.getName());
        gp.setLocationLat(req.getLocationLat());
        gp.setLocationLng(req.getLocationLng());
        gp.setSchedule(req.getSchedule());
        gp.setCapacity(req.getCapacity());

        // Resuelve los IDs de materiales a entidades
        if (req.getAcceptedMaterialIds() != null && !req.getAcceptedMaterialIds().isEmpty()) {
            List<MaterialCatalog> materials = materialRepo.findAllById(req.getAcceptedMaterialIds());
            gp.setAcceptedMaterials(new ArrayList<>(materials));
        } else {
            gp.setAcceptedMaterials(new ArrayList<>());
        }
    }

    public GreenPointResponse toResponse(GreenPoint gp) {
        GreenPointResponse r = new GreenPointResponse();
        r.setId(gp.getId());
        r.setTenantId(gp.getTenantId());
        r.setName(gp.getName());
        r.setLocationLat(gp.getLocationLat());
        r.setLocationLng(gp.getLocationLng());
        r.setSchedule(gp.getSchedule());
        r.setCapacity(gp.getCapacity());
        r.setStatus(gp.getStatus());
        if (gp.getAcceptedMaterials() != null) {
            r.setAcceptedMaterials(gp.getAcceptedMaterials()
                    .stream()
                    .map(materialService::toResponse)
                    .collect(Collectors.toList()));
        }
        return r;
    }
}