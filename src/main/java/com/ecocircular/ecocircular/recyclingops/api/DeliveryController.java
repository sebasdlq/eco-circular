package com.ecocircular.ecocircular.recyclingops.api;

import com.ecocircular.ecocircular.recyclingops.application.DeliveryService;
import com.ecocircular.ecocircular.recyclingops.domain.Delivery;
import com.ecocircular.ecocircular.recyclingops.domain.DeliveryDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    // POST /api/deliveries
    @PostMapping
    public ResponseEntity<Delivery> crear(@RequestBody Delivery delivery) {
        Delivery creada = deliveryService.crear(delivery);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    // GET /api/deliveries/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Delivery> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(deliveryService.obtenerPorId(id));
    }

    // PATCH /api/deliveries/{id}/validar
    @PatchMapping("/{id}/validar")
    public ResponseEntity<Delivery> validar(@PathVariable UUID id) {
        return ResponseEntity.ok(deliveryService.validar(id));
    }

    // PATCH /api/deliveries/{id}/ajustar
    @PatchMapping("/{id}/ajustar")
    public ResponseEntity<Delivery> ajustar(
            @PathVariable UUID id,
            @RequestBody List<DeliveryDetail> newDetails) {
        return ResponseEntity.ok(deliveryService.ajustarCantidades(id, newDetails));
    }

    // DELETE /api/deliveries/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        deliveryService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}