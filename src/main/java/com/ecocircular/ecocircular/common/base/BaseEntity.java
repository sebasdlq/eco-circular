package com.ecocircular.ecocircular.common.base;

import com.ecocircular.ecocircular.common.audit.AuditContext;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BaseEntity actualizada con campos de auditoría estándar.
 *
 * Agrega updated_at, updated_by y created_by a todas las entidades
 * siguiendo el esquema del PDF:
 *   "created_at, updated_at, created_by, updated_by"
 */
@MappedSuperclass
@Getter @Setter
public abstract class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    // ── Campos de auditoría base ──────────────────────────────────────────
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** UUID del usuario que creó el registro */
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    /** UUID del usuario que hizo la última modificación */
    @Column(name = "updated_by")
    private UUID updatedBy;

    @PrePersist
    public void prePersist() {
        if (tenantId == null) {
            tenantId = TenantContext.getTenantId();
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.createdBy = AuditContext.getActorId();   // quién creó
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = AuditContext.getActorId();   // quién modificó
    }
}