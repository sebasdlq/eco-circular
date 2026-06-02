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
    @PreAuthorize("hasAuthority('VALIDATE_DELIVERY') or hasAuthority('VIEW_ALL_ANALYTICS')")
    public ResponseEntity<List<DeliveryResponse>> listAllByTenant() {
        List<DeliveryResponse> deliveries = deliveryService.getAllByCurrentTenant();
        return ResponseEntity.ok(deliveries);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VALIDATE_DELIVERY')")
    public ResponseEntity<DeliveryResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(deliveryService.obtenerPorId(id));
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