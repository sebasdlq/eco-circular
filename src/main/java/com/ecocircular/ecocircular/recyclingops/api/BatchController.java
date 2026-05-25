package com.ecocircular.ecocircular.recyclingops.api;

import com.ecocircular.ecocircular.recyclingops.application.BatchService;
import com.ecocircular.ecocircular.recyclingops.domain.Batch;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/batches")
public class BatchController {

    private final BatchService service;

    public BatchController(BatchService service) {
        this.service = service;
    }

    @PostMapping
    public Batch create(@RequestBody Batch batch) {
        return service.create(batch);
    }

    @GetMapping("/{id}")
    public Batch read(@PathVariable UUID id) {
        return service.read(id);
    }

    @PutMapping("/{id}/dispatch")
    public Batch dispatch(@PathVariable UUID id) {
        return service.dispatch(id);
    }

    @PutMapping("/{id}/receive")
    public Batch receive(@PathVariable UUID id) {
        return service.receive(id);
    }
}