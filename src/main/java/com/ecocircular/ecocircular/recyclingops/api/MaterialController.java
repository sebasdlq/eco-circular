package com.ecocircular.ecocircular.recyclingops.api;

import com.ecocircular.ecocircular.recyclingops.application.MaterialService;
import com.ecocircular.ecocircular.recyclingops.domain.MaterialCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {
    private final MaterialService service;

    @GetMapping
    public List<MaterialCatalog> list() {
        return service.getMaterialsForCurrentTenant();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_GREEN_POINTS')") // según PDF
    @ResponseStatus(HttpStatus.CREATED)
    public MaterialCatalog create(@RequestBody MaterialCatalog material) {
        return service.createMaterial(material);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_GREEN_POINTS')")
    public MaterialCatalog update(@PathVariable UUID id, @RequestBody MaterialCatalog material) {
        return service.updateMaterial(id, material);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_GREEN_POINTS')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID id) {
        service.deactivateMaterial(id);
    }
}
