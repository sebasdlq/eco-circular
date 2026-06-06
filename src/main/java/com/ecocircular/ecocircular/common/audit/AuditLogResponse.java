package com.ecocircular.ecocircular.common.audit;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para los endpoints de auditoría.
 * No expone beforeState/afterState completos en el listado general
 * para evitar payloads masivos — se puede agregar un endpoint de detalle.
 */
@Data
public class AuditLogResponse {

    private UUID          id;
    private String        entityType;
    private UUID          entityId;
    private String        eventType;
    private String        actorName;
    private String        reason;
    private String        clientIp;
    private LocalDateTime createdAt;

    /** Incluye snapshots JSON para el detalle de un evento específico */
    private String beforeState;
    private String afterState;

    public static AuditLogResponse from(AuditLog log) {
        AuditLogResponse dto = new AuditLogResponse();
        dto.setId(log.getId());
        dto.setEntityType(log.getEntityType());
        dto.setEntityId(log.getEntityId());
        dto.setEventType(log.getEventType());
        dto.setActorName(log.getActorName());
        dto.setReason(log.getReason());
        dto.setClientIp(log.getClientIp());
        dto.setCreatedAt(log.getCreatedAt());
        dto.setBeforeState(log.getBeforeState());
        dto.setAfterState(log.getAfterState());
        return dto;
    }
}
