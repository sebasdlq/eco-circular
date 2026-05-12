package com.ecocircular.ecocircular.gamification.infrastructure.adapter;

import com.ecocircular.ecocircular.gamification.application.port.GamificationActivityPort;
import com.ecocircular.ecocircular.gamification.application.port.UserRecyclingActivity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * TEMPORARY MOCK ADAPTER — Dev 5 use only.
 *
 * This adapter simulates the recycling activity data that will be provided
 * by Dev 2 once the Entrega and EntregaDetalle modules are implemented.
 *
 * OWNERSHIP BOUNDARIES:
 *  - Dev 5 owns: levels, badges, missions, ranking, recommendations (GamificationService and above).
 *  - Dev 2 owns: delivery creation, validation, QR flow, base points calculation, CO2 calculation.
 *  - This class MUST be replaced by a real adapter once Dev 2 exposes delivery data.
 *  - Replacement requires only: implement GamificationActivityPort in a new class that reads from
 *    EntregaRepository / EntregaDetalleRepository, then remove this @Component annotation.
 *  - No changes to GamificationActivityPort, GamificationService, or any other Dev 5 class are needed.
 *
 * Mock data matches the sample users from the project database schema (Basededatos.txt).
 */
@Component
public class MockGamificationActivityAdapter implements GamificationActivityPort {

    private static final UUID TENANT_CENTRO = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final List<UserRecyclingActivity> MOCK_DATA = List.of(

            // Juan Pérez — CIUDADANO — nivel ORO (580 pts)
            new UserRecyclingActivity(
                    UUID.fromString("99999999-9999-9999-9999-999999999999"),
                    "Juan Pérez", "CIUDADANO", TENANT_CENTRO,
                    12, 48.5, 580.0, 19.4,
                    Map.of("Plástico PET", 20.0, "Papel y Cartón", 15.5, "Vidrio", 13.0),
                    5, LocalDate.of(2026, 5, 8)
            ),

            // María López — ESTUDIANTE — nivel PLATA (210 pts)
            new UserRecyclingActivity(
                    UUID.fromString("88888888-8888-8888-8888-888888888888"),
                    "María López", "ESTUDIANTE", TENANT_CENTRO,
                    6, 22.0, 210.0, 7.8,
                    Map.of("Plástico PET", 10.0, "Papel y Cartón", 12.0),
                    2, LocalDate.of(2026, 5, 5)
            ),

            // Carlos Ruiz — OPERADOR — nivel BRONCE (45 pts, operador no recicla frecuente)
            new UserRecyclingActivity(
                    UUID.fromString("77777777-7777-7777-7777-777777777777"),
                    "Carlos Ruiz", "OPERADOR", TENANT_CENTRO,
                    2, 4.5, 45.0, 1.8,
                    Map.of("Metal (Aluminio)", 4.5),
                    8, LocalDate.of(2026, 4, 20)
            ),

            // Ana Gómez — MUNICIPALIDAD — nivel PLATINO (1620 pts)
            new UserRecyclingActivity(
                    UUID.fromString("66666666-6666-6666-6666-666666666666"),
                    "Ana Gómez", "MUNICIPALIDAD", TENANT_CENTRO,
                    38, 120.0, 1620.0, 72.0,
                    Map.of("Plástico PET", 40.0, "Papel y Cartón", 35.0,
                            "Vidrio", 25.0, "Metal (Aluminio)", 20.0),
                    14, LocalDate.of(2026, 5, 10)
            ),

            // Luis Fernández — DOCENTE — nivel PLATA (340 pts)
            new UserRecyclingActivity(
                    UUID.fromString("55555555-5555-5555-5555-555555555555"),
                    "Luis Fernández", "DOCENTE", TENANT_CENTRO,
                    9, 35.0, 340.0, 14.5,
                    Map.of("Papel y Cartón", 20.0, "Plástico PET", 10.0, "Vidrio", 5.0),
                    4, LocalDate.of(2026, 5, 3)
            ),

            // Pedro Sánchez — ESTUDIANTE — nivel BRONCE (80 pts, nuevo usuario)
            new UserRecyclingActivity(
                    UUID.fromString("44444444-4444-4444-4444-444444444444"),
                    "Pedro Sánchez", "ESTUDIANTE", TENANT_CENTRO,
                    3, 8.0, 80.0, 3.2,
                    Map.of("Plástico PET", 5.0, "Papel y Cartón", 3.0),
                    1, LocalDate.of(2026, 4, 28)
            )
    );

    @Override
    public UserRecyclingActivity getUserRecyclingActivity(UUID tenantId, UUID userId) {
        return MOCK_DATA.stream()
                .filter(a -> a.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado en datos mock: " + userId
                                + ". Reemplazar MockGamificationActivityAdapter por adaptador real cuando Dev 2 entregue Entrega/EntregaDetalle."));
    }

    @Override
    public List<UserRecyclingActivity> getAllUsersRecyclingActivity(UUID tenantId) {
        return MOCK_DATA;
    }
}
