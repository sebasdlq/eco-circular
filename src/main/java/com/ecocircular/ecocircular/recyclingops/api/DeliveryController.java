package com.ecocircular.ecocircular.recyclingops.api;

import com.ecocircular.ecocircular.common.audit.AuditContext;
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

    @GetMapping
    public ResponseEntity<List<DeliveryResponse>> listar() {
        return ResponseEntity.ok(deliveryService.getAllByCurrentTenant());
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<DeliveryResponse>> listarPorUsuario(@PathVariable UUID id) {
        return ResponseEntity.ok(deliveryService.obtenerPorUserId(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(deliveryService.obtenerPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<DeliveryResponse> crear(@Valid @RequestBody DeliveryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deliveryService.crear(request));
    }

    @PostMapping("/{id}/validar")
    public ResponseEntity<DeliveryResponse> validar(@PathVariable UUID id) {
        return ResponseEntity.ok(deliveryService.validar(id));
    }

    /**
     * Ajuste de cantidades — operación crítica.
     * Requiere cabecera X-Reason con la justificación del ajuste.
     * Solo auditores pueden realizar esta operación.
     */
    @PatchMapping("/{id}/ajustar")
    @PreAuthorize("hasAuthority('ROLE_AUDITOR')")
    public ResponseEntity<DeliveryResponse> ajustar(
            @PathVariable UUID id,
            @RequestHeader("X-Reason") String reason,
            @Valid @RequestBody List<DeliveryDetailRequest> newDetails) {

        AuditContext.setReason(reason);
        try {
            return ResponseEntity.ok(deliveryService.ajustarCantidades(id, newDetails));
        } finally {
            AuditContext.clear();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MUNICIPALITY_ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        deliveryService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}