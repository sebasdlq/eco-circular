package com.ecocircular.ecocircular.gamification.application;

import com.ecocircular.ecocircular.common.audit.AuditContext;
import com.ecocircular.ecocircular.common.audit.AuditEvents;
import com.ecocircular.ecocircular.common.audit.AuditService;
import com.ecocircular.ecocircular.gamification.api.dto.MissionResponse;
import com.ecocircular.ecocircular.gamification.application.port.UserRecyclingActivity;
import com.ecocircular.ecocircular.gamification.domain.Mission;
import com.ecocircular.ecocircular.gamification.domain.MissionStatus;
import com.ecocircular.ecocircular.gamification.domain.UserMission;
import com.ecocircular.ecocircular.gamification.infrastructure.persistence.MissionRepository;
import com.ecocircular.ecocircular.gamification.infrastructure.persistence.UserMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class MissionEvaluator {

    private final MissionRepository     missionRepository;
    private final UserMissionRepository userMissionRepository;
    private final AuditService          auditService;

    /**
     * Evalúa el progreso del usuario en todas las misiones activas del tenant.
     * Crea o actualiza el registro de UserMission según el estado actual.
     * Devuelve la lista completa de misiones del usuario en el tenant.
     */
    @Transactional
    public List<MissionResponse> evaluate(UserRecyclingActivity activity, UUID tenantId) {

        List<Mission> catalog = missionRepository.findAvailableForTenant(tenantId);

        for (Mission mission : catalog) {
            try {
                procesarMision(mission, activity, tenantId);
            } catch (Exception ex) {
                log.warn("[MissionEvaluator] Misión ya procesada concurrentemente: {}", mission.getId());
            }
        }

        return userMissionRepository.findByUserIdAndTenantId(activity.getUserId(), tenantId)
                .stream()
                .map(MissionResponse::from)
                .toList();
    }
    private void procesarMision(Mission mission, UserRecyclingActivity activity, UUID tenantId) {
        double current = resolveCurrentValue(mission, activity);
        double target  = mission.getTargetValue();
        int    pct     = (int) Math.min(100, Math.round((current / target) * 100));

        LocalDate deadline = mission.getDeadline() != null
                ? mission.getDeadline()
                : LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        MissionStatus newStatus = current >= target
                ? MissionStatus.COMPLETADA
                : (LocalDate.now().isAfter(deadline) ? MissionStatus.EXPIRADA : MissionStatus.EN_PROGRESO);

        Optional<UserMission> existing = userMissionRepository
                .findByUserIdAndMission_IdAndTenantId(activity.getUserId(), mission.getId(), tenantId);

        if (existing.isEmpty()) {
            UserMission um = new UserMission(
                    activity.getUserId(), mission, newStatus, current, pct, deadline);
            um.setTenantId(tenantId);
            userMissionRepository.save(um);

            if (newStatus == MissionStatus.COMPLETADA) {
                auditarMisionCompletada(um, tenantId);
            }
        } else {
            UserMission um = existing.get();
            if (um.getStatus() == MissionStatus.EN_PROGRESO) {
                um.setCurrentValue(current);
                um.setProgressPercent(pct);
                um.setStatus(newStatus);
                if (newStatus == MissionStatus.COMPLETADA) {
                    um.setCompletedAt(java.time.LocalDateTime.now());
                    auditarMisionCompletada(um, tenantId);
                }
                userMissionRepository.save(um);
            }
        }
    }

    private double resolveCurrentValue(Mission mission, UserRecyclingActivity activity) {
        return switch (mission.getTargetType()) {
            case "DELIVERIES"         -> Math.min(activity.getTotalDeliveries(),            mission.getTargetValue());
            case "KG_RECYCLED"        -> Math.min(activity.getTotalKgRecycled(),            mission.getTargetValue());
            case "GREEN_POINT_VISITS" -> Math.min(activity.getGreenPointVisits(),           mission.getTargetValue());
            case "MATERIALS_VARIETY"  -> Math.min(activity.getMaterialsRecycled().size(),   mission.getTargetValue());
            default                   -> 0.0;
        };
    }

    private void auditarMisionCompletada(UserMission um, UUID tenantId) {
        auditService.registrar(
                "UserMission", um.getId(),
                AuditEvents.MISION_COMPLETADA,
                null,
                new MisionSnapshot(um.getUserId(), um.getMission().getId(), um.getMission().getName()),
                tenantId,
                AuditContext.getActorId(),
                AuditContext.getActorName(),
                AuditContext.getClientIp(),
                null
        );
    }

    record MisionSnapshot(UUID userId, UUID missionId, String missionName) {}
}
