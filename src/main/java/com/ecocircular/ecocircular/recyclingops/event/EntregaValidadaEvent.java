package com.ecocircular.ecocircular.recyclingops.event;

import java.util.UUID;

/**
 * Evento publicado por DeliveryService cuando una entrega pasa a VALIDATED.
 * GamificationService lo escucha para disparar la evaluación de badges y misiones.
 *
 * No extiende ApplicationEvent para mantenerlo como un record simple
 * compatible con @EventListener de Spring.
 */
public record EntregaValidadaEvent(UUID userId, UUID tenantId) {}
