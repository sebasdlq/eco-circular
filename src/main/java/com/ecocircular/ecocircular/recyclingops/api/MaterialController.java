package com.ecocircular.ecocircular.recyclingops.api;

import com.ecocircular.ecocircular.recyclingops.application.MaterialService;
import com.ecocircular.ecocircular.recyclingops.dto.MaterialRequest;
import com.ecocircular.ecocircular.recyclingops.dto.MaterialResponse;
import jakarta.validation.Valid;
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
    public List<MaterialResponse> list() {
        return service.getMaterialsForCurrentTenant();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_GREEN_POINTS') or has('ROLE_MUNICIPALITY_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public MaterialResponse create(@Valid @RequestBody MaterialRequest request) {
        return service.createMaterial(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_GREEN_POINTS')")
    public MaterialResponse update(@PathVariable UUID id,
                                   @Valid @RequestBody MaterialRequest request) {
        return service.updateMaterial(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_GREEN_POINTS')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID id) {
        service.deactivateMaterial(id);
    }
}