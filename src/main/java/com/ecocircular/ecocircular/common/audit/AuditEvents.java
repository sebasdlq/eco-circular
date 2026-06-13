package com.ecocircular.ecocircular.common.audit;

/**
 * Catálogo de eventos de dominio auditables.
 *
 * Nomenclatura: ENTIDAD_ACCION (en español, siguiendo el lenguaje ubicuo del PDF).
 * Estos nombres se almacenan tal cual en audit_logs.event_type.
 */
public final class AuditEvents {

    private AuditEvents() {}

    // ── Delivery (Entrega) ────────────────────────────────────────────────
    public static final String ENTREGA_CREADA    = "ENTREGA_CREADA";
    public static final String ENTREGA_VALIDADA  = "ENTREGA_VALIDADA";
    public static final String ENTREGA_AJUSTADA  = "ENTREGA_AJUSTADA";
    public static final String ENTREGA_ELIMINADA = "ENTREGA_ELIMINADA";
    public static final String PUNTOS_ASIGNADOS  = "PUNTOS_ASIGNADOS";

    // ── Batch (Lote) ──────────────────────────────────────────────────────
    public static final String LOTE_ABIERTO    = "LOTE_ABIERTO";
    public static final String LOTE_CERRADO    = "LOTE_CERRADO";
    public static final String LOTE_DESPACHADO = "LOTE_DESPACHADO";

    // ── GreenPoint (Punto Verde) ──────────────────────────────────────────
    public static final String PUNTO_VERDE_CREADO       = "PUNTO_VERDE_CREADO";
    public static final String PUNTO_VERDE_ACTUALIZADO  = "PUNTO_VERDE_ACTUALIZADO";
    public static final String PUNTO_VERDE_DESACTIVADO  = "PUNTO_VERDE_DESACTIVADO";

    // ── IAM ───────────────────────────────────────────────────────────────
    public static final String USUARIO_CREADO          = "USUARIO_CREADO";
    public static final String USUARIO_ROL_ASIGNADO    = "USUARIO_ROL_ASIGNADO";
    public static final String USUARIO_DESACTIVADO     = "USUARIO_DESACTIVADO";
    public static final String SESION_INICIADA         = "SESION_INICIADA";
    public static final String SESION_FALLIDA          = "SESION_FALLIDA";

    // ── Gamificación ──────────────────────────────────────────────────────
    public static final String MISION_COMPLETADA   = "MISION_COMPLETADA";
    public static final String BADGE_OTORGADO      = "BADGE_OTORGADO";
    public static final String RANKING_ACTUALIZADO = "RANKING_ACTUALIZADO";
    public static final String BADGE_CREADO  = "BADGE_CREADO";
    public static final String MISION_CREADA = "MISION_CREADA";

    // ── Campaña ───────────────────────────────────────────────────────────
    public static final String CAMPANA_PUBLICADA = "CAMPANA_PUBLICADA";
    public static final String CAMPANA_CERRADA   = "CAMPANA_CERRADA";
}
