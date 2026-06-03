package com.ecocircular.ecocircular.common.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registro inmutable de auditoría.
 * NUNCA se edita ni se borra — solo se inserta.
 *
 * Cubre el requerimiento del PDF:
 *   "Quién cambió qué, cuándo y por qué (razón obligatoria).
 *    Correcciones como nuevo evento, nunca editar el pasado."
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_entity",     columnList = "entity_type, entity_id"),
    @Index(name = "idx_audit_tenant",     columnList = "tenant_id"),
    @Index(name = "idx_audit_created_at", columnList = "created_at"),
    @Index(name = "idx_audit_actor",      columnList = "actor_id")
})
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Tenant al que pertenece el evento */
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    /** Nombre de la entidad de dominio: "Delivery", "Batch", "GreenPoint", etc. */
    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    /** ID del agregado afectado */
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    /**
     * Evento de dominio disparado.
     * Ejemplos: ENTREGA_CREADA, ENTREGA_VALIDADA, ENTREGA_AJUSTADA,
     *           LOTE_CERRADO, PUNTOS_ASIGNADOS
     */
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    /** ID del usuario que ejecutó la acción (null si es proceso interno) */
    @Column(name = "actor_id")
    private UUID actorId;

    /** Email / nombre del actor en el momento del evento (desnormalizado para trazabilidad) */
    @Column(name = "actor_name", length = 150)
    private String actorName;

    /**
     * Razón obligatoria para operaciones críticas (ajustes, correcciones, premios).
     * Para eventos de creación/validación puede ser null.
     */
    @Column(name = "reason", length = 500)
    private String reason;

    /** Snapshot JSON del estado ANTERIOR (null en creación) */
    @Column(name = "before_state", columnDefinition = "TEXT")
    private String beforeState;

    /** Snapshot JSON del estado POSTERIOR */
    @Column(name = "after_state", columnDefinition = "TEXT")
    private String afterState;

    /** IP del cliente (para auditoría forense) */
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
