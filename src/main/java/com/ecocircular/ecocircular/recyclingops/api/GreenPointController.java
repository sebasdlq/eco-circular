package com.ecocircular.ecocircular.recyclingops.api;

import com.ecocircular.ecocircular.recyclingops.application.GreenPointService;
import com.ecocircular.ecocircular.recyclingops.dto.GreenPointRequest;
import com.ecocircular.ecocircular.recyclingops.dto.GreenPointResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/green-points")
@RequiredArgsConstructor
public class GreenPointController {

    private final GreenPointService service;

    @GetMapping
    public List<GreenPointResponse> list() {
        return service.getAllForTenant();
    }

    @GetMapping("/{id}")
    public GreenPointResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_GREEN_POINTS')")
    @ResponseStatus(HttpStatus.CREATED)
    public GreenPointResponse create(@Valid @RequestBody GreenPointRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_GREEN_POINTS')")
    public GreenPointResponse update(@PathVariable UUID id,
                                     @Valid @RequestBody GreenPointRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('MANAGE_GREEN_POINTS')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable UUID id) {
        service.disable(id);
    }
}