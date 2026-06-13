package com.ecocircular.ecocircular.gamification.application;

import com.ecocircular.ecocircular.common.audit.AuditContext;
import com.ecocircular.ecocircular.common.audit.AuditEvents;
import com.ecocircular.ecocircular.common.audit.AuditService;
import com.ecocircular.ecocircular.gamification.api.dto.BadgeRequest;
import com.ecocircular.ecocircular.gamification.api.dto.BadgeResponse;
import com.ecocircular.ecocircular.gamification.api.dto.MissionRequest;
import com.ecocircular.ecocircular.gamification.api.dto.MissionResponse;
import com.ecocircular.ecocircular.gamification.domain.Badge;
import com.ecocircular.ecocircular.gamification.domain.Mission;
import com.ecocircular.ecocircular.gamification.infrastructure.persistence.BadgeRepository;
import com.ecocircular.ecocircular.gamification.infrastructure.persistence.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GamificationAdminService {

    private final BadgeRepository   badgeRepository;
    private final MissionRepository missionRepository;
    private final AuditService      auditService;

    // ── Badges ────────────────────────────────────────────────────────────────

    public List<BadgeResponse> listBadges(UUID tenantId) {
        return badgeRepository.findAvailableForTenant(tenantId)
                .stream().map(BadgeResponse::fromEntity).toList();
    }

    @Transactional
    public BadgeResponse createBadge(BadgeRequest req, UUID tenantId) {
        Badge badge = new Badge(req.getName(), req.getDescription(), req.getIconCode());
        badge.setTenantId(req.getTenantId() != null ? req.getTenantId() : tenantId);
        Badge saved = badgeRepository.save(badge);

        auditService.registrar(
                "Badge", saved.getId(), AuditEvents.BADGE_CREADO,
                null, new BadgeSnapshot(saved.getId(), saved.getName()),
                tenantId,
                AuditContext.getActorId(), AuditContext.getActorName(),
                AuditContext.getClientIp(), null
        );
        return BadgeResponse.fromEntity(saved);
    }

    @Transactional
    public BadgeResponse updateBadge(UUID id, BadgeRequest req) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Badge no encontrado: " + id));
        badge.setName(req.getName());
        badge.setDescription(req.getDescription());
        badge.setIconCode(req.getIconCode());
        return BadgeResponse.fromEntity(badgeRepository.save(badge));
    }

    @Transactional
    public void deactivateBadge(UUID id) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Badge no encontrado: " + id));
        badge.setActive(false);
        badgeRepository.save(badge);
    }

    // ── Missions ──────────────────────────────────────────────────────────────

    public List<MissionResponse> listMissions(UUID tenantId) {
        return missionRepository.findAvailableForTenant(tenantId)
                .stream().map(MissionResponse::fromEntity).toList();
    }

    @Transactional
    public MissionResponse createMission(MissionRequest req, UUID tenantId) {
        Mission mission = new Mission(
                req.getName(), req.getDescription(),
                req.getTargetType(), req.getTargetValue(),
                req.getRewardPoints(), req.getDeadline()
        );
        mission.setTenantId(req.getTenantId() != null ? req.getTenantId() : tenantId);
        Mission saved = missionRepository.save(mission);

        auditService.registrar(
                "Mission", saved.getId(), AuditEvents.MISION_CREADA,
                null, new MisionSnapshot(saved.getId(), saved.getName()),
                tenantId,
                AuditContext.getActorId(), AuditContext.getActorName(),
                AuditContext.getClientIp(), null
        );
        return MissionResponse.fromEntity(saved);
    }

    @Transactional
    public MissionResponse updateMission(UUID id, MissionRequest req) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Misión no encontrada: " + id));
        mission.setName(req.getName());
        mission.setDescription(req.getDescription());
        mission.setTargetType(req.getTargetType());
        mission.setTargetValue(req.getTargetValue());
        mission.setRewardPoints(req.getRewardPoints());
        mission.setDeadline(req.getDeadline());
        return MissionResponse.fromEntity(missionRepository.save(mission));
    }

    @Transactional
    public void deactivateMission(UUID id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Misión no encontrada: " + id));
        mission.setActive(false);
        missionRepository.save(mission);
    }

    record BadgeSnapshot(UUID id, String name) {}
    record MisionSnapshot(UUID id, String name) {}
}