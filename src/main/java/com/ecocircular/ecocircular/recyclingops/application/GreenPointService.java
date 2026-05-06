package com.ecocircular.ecocircular.recyclingops.application;

import com.ecocircular.ecocircular.common.base.TenantContext;
import com.ecocircular.ecocircular.recyclingops.domain.GreenPoint;
import com.ecocircular.ecocircular.recyclingops.infrastructure.persistence.GreenPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GreenPointService {
    private final GreenPointRepository repo;

    public List<GreenPoint> getAllForTenant() {
        return repo.findByTenantId(TenantContext.getTenantId());
    }

    @Transactional
    public GreenPoint create(GreenPoint gp) {
        gp.setTenantId(TenantContext.getTenantId());
        gp.setStatus("ACTIVE");
        return repo.save(gp);
    }

    @Transactional
    public GreenPoint update(UUID id, GreenPoint updated) {
        GreenPoint gp = repo.findById(id).orElseThrow();
        gp.setName(updated.getName());
        gp.setLocationLat(updated.getLocationLat());
        gp.setLocationLng(updated.getLocationLng());
        gp.setSchedule(updated.getSchedule());
        gp.setCapacity(updated.getCapacity());
        gp.setAcceptedMaterials(updated.getAcceptedMaterials());
        return repo.save(gp);
    }

    @Transactional
    public void disable(UUID id) {
        GreenPoint gp = repo.findById(id).orElseThrow();
        gp.setStatus("INACTIVE");
        repo.save(gp);
    }
}
