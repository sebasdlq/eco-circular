package com.ecocircular.ecocircular.common.audit;

import com.ecocircular.ecocircular.common.base.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Endpoints de auditoría — solo roles con permisos de supervisión.
 *
 * GET /api/v1/audit/entity/{type}/{id}   → timeline de un agregado
 * GET /api/v1/audit                       → log del tenant, paginado
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'MUNICIPALIDAD')")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    /**
     * Timeline completo de un agregado.
     * Ejemplo: GET /api/v1/audit/entity/Delivery/3fa85f64-5717-4562-b3fc-2c963f66afa6
     */
    @GetMapping("/entity/{type}/{id}")
    public ResponseEntity<List<AuditLogResponse>> timelineEntidad(
            @PathVariable("type") String entityType,
            @PathVariable("id")   UUID   entityId) {

        List<AuditLogResponse> result = auditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtAsc(entityType, entityId)
                .stream()
                .map(AuditLogResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Log completo del tenant con paginación.
     * Ejemplo: GET /api/v1/audit?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> logTenant(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        UUID tenantId = TenantContext.getTenantId();
        Page<AuditLogResponse> page = auditLogRepository
                .findByTenantIdOrderByCreatedAtDesc(tenantId, pageable)
                .map(AuditLogResponse::from);

        return ResponseEntity.ok(page);
    }
}
