package com.ecocircular.ecocircular.recyclingops.api;

import com.ecocircular.ecocircular.recyclingops.application.DeliveryService;
import com.ecocircular.ecocircular.recyclingops.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<DeliveryResponse> crear(@Valid @RequestBody DeliveryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryService.crear(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('GREEN_POINT_ADMIN') or hasRole('MUNICIPALITY_ADMIN')")
    public ResponseEntity<List<DeliveryResponse>> listAllByTenant() {
        List<DeliveryResponse> deliveries = deliveryService.getAllByCurrentTenant();
        return ResponseEntity.ok(deliveries);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('GREEN_POINT_ADMIN')")
    public ResponseEntity<DeliveryResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(deliveryService.obtenerPorId(id));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<DeliveryResponse>> obtenerPorUser(@PathVariable UUID id) {
        return ResponseEntity.ok(deliveryService.obtenerPorUserId(id));
    }

    @PatchMapping("/{id}/validar")
    public ResponseEntity<DeliveryResponse> validar(@PathVariable UUID id) {
        return ResponseEntity.ok(deliveryService.validar(id));
    }

    @PatchMapping("/{id}/ajustar")
    public ResponseEntity<DeliveryResponse> ajustar(
            @PathVariable UUID id,
            @Valid @RequestBody List<DeliveryDetailRequest> newDetails) {
        return ResponseEntity.ok(deliveryService.ajustarCantidades(id, newDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        deliveryService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}