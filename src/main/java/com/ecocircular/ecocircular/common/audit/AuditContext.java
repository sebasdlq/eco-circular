package com.ecocircular.ecocircular.common.audit;

import java.util.UUID;

/**
 * Contexto de auditoría por hilo de ejecución.
 * Se puebla en el filtro JWT igual que TenantContext.
 *
 * Uso:
 *   AuditContext.setActor(userId, "maria@eco.com", "192.168.1.1");
 *   AuditContext.setReason("Corrección de peso por error de báscula");
 */
public class AuditContext {

    private static final ThreadLocal<UUID>   actorId   = new ThreadLocal<>();
    private static final ThreadLocal<String> actorName = new ThreadLocal<>();
    private static final ThreadLocal<String> clientIp  = new ThreadLocal<>();
    private static final ThreadLocal<String> reason    = new ThreadLocal<>();

    public static void setActor(UUID id, String name, String ip) {
        actorId.set(id);
        actorName.set(name);
        clientIp.set(ip);
    }

    public static void setReason(String r) { reason.set(r); }

    public static UUID   getActorId()   { return actorId.get(); }
    public static String getActorName() { return actorName.get(); }
    public static String getClientIp()  { return clientIp.get(); }
    public static String getReason()    { return reason.get(); }

    /** Llamar al final del request para evitar memory leaks */
    public static void clear() {
        actorId.remove();
        actorName.remove();
        clientIp.remove();
        reason.remove();
    }
}
