package com.ecocircular.ecocircular.common.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /** Historial completo de un agregado específico (ej: timeline de una entrega) */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtAsc(
            String entityType, UUID entityId);

    /** Todos los eventos de un tenant, paginados para el panel ejecutivo */
    Page<AuditLog> findByTenantIdOrderByCreatedAtDesc(UUID tenantId, Pageable pageable);

    /** Acciones de un actor específico en un rango de fechas */
    @Query("""
        SELECT a FROM AuditLog a
        WHERE a.actorId = :actorId
          AND a.createdAt BETWEEN :from AND :to
        ORDER BY a.createdAt DESC
    """)
    List<AuditLog> findByActorAndDateRange(
            @Param("actorId") UUID actorId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    /** Detección de anomalías: picos de un tipo de evento en poco tiempo */
    @Query("""
        SELECT a FROM AuditLog a
        WHERE a.tenantId  = :tenantId
          AND a.eventType = :eventType
          AND a.createdAt >= :since
        ORDER BY a.createdAt DESC
    """)
    List<AuditLog> findRecentByEventType(
            @Param("tenantId")  UUID tenantId,
            @Param("eventType") String eventType,
            @Param("since")     LocalDateTime since);
}
