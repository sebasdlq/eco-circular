package com.ecocircular.ecocircular.common.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper        objectMapper;

    /**
     * Registra un evento de dominio.
     * Síncrono — el tenantId, actorId y actorName se capturan
     * en el hilo del request antes de cualquier @Async.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String entityType,
                          UUID   entityId,
                          String eventType,
                          Object before,
                          Object after,
                          UUID   tenantId,    // ← capturado en el hilo del request
                          UUID   actorId,     // ← capturado en el hilo del request
                          String actorName,   // ← capturado en el hilo del request
                          String clientIp,
                          String reason) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setTenantId(tenantId);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setEventType(eventType);
            auditLog.setActorId(actorId);
            auditLog.setActorName(actorName);
            auditLog.setClientIp(clientIp);
            auditLog.setReason(reason);
            auditLog.setBeforeState(before != null ? toJson(before) : null);
            auditLog.setAfterState(after   != null ? toJson(after)  : null);

            auditLogRepository.save(auditLog);

        } catch (Exception ex) {
            log.error("[AUDIT] Error al registrar evento {} para {}/{}: {}",
                    eventType, entityType, entityId, ex.getMessage());
        }
    }

    /** Versión síncrona para operaciones que requieren garantía inmediata */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarSync(String entityType,
                              UUID   entityId,
                              String eventType,
                              Object before,
                              Object after,
                              UUID   tenantId,
                              UUID   actorId,
                              String actorName,
                              String clientIp,
                              String reason) {
        registrar(entityType, entityId, eventType, before, after,
                tenantId, actorId, actorName, clientIp, reason);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{\"error\":\"no_serializable\"}";
        }
    }
}