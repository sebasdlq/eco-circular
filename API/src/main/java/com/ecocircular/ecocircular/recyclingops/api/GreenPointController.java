package com.ecocircular.ecocircular.recyclingops.api;

import com.ecocircular.ecocircular.recyclingops.application.GreenPointService;
import com.ecocircular.ecocircular.recyclingops.domain.GreenPoint;
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
    public List<GreenPoint> list() {
        return service.getAllForTenant();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_GREEN_POINTS')")
    @ResponseStatus(HttpStatus.CREATED)
    public GreenPoint create(@RequestBody GreenPoint gp) {
        return service.create(gp);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_GREEN_POINTS')")
    public GreenPoint update(@PathVariable UUID id, @RequestBody GreenPoint gp) {
        return service.update(id, gp);
    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('MANAGE_GREEN_POINTS')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable UUID id) {
        service.disable(id);
    }
}