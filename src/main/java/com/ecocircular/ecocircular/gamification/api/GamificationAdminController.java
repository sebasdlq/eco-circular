package com.ecocircular.ecocircular.gamification.api;

import com.ecocircular.ecocircular.common.base.TenantContext;
import com.ecocircular.ecocircular.gamification.api.dto.BadgeRequest;
import com.ecocircular.ecocircular.gamification.api.dto.BadgeResponse;
import com.ecocircular.ecocircular.gamification.api.dto.MissionRequest;
import com.ecocircular.ecocircular.gamification.api.dto.MissionResponse;
import com.ecocircular.ecocircular.gamification.application.GamificationAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gamification/admin")
@RequiredArgsConstructor
public class GamificationAdminController {

    private final GamificationAdminService adminService;

    // ── Badges ────────────────────────────────────────────────────────────────

    @GetMapping("/badges")
    public List<BadgeResponse> listBadges() {
        return adminService.listBadges(TenantContext.getTenantId());
    }

    @PostMapping("/badges")
    @ResponseStatus(HttpStatus.CREATED)
    public BadgeResponse createBadge(@Valid @RequestBody BadgeRequest req) {
        return adminService.createBadge(req, TenantContext.getTenantId());
    }

    @PutMapping("/badges/{id}")
    public BadgeResponse updateBadge(@PathVariable UUID id,
                                     @Valid @RequestBody BadgeRequest req) {
        return adminService.updateBadge(id, req);
    }

    @DeleteMapping("/badges/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateBadge(@PathVariable UUID id) {
        adminService.deactivateBadge(id);
    }

    // ── Missions ──────────────────────────────────────────────────────────────

    @GetMapping("/missions")
    public List<MissionResponse> listMissions() {
        return adminService.listMissions(TenantContext.getTenantId());
    }

    @PostMapping("/missions")
    @ResponseStatus(HttpStatus.CREATED)
    public MissionResponse createMission(@Valid @RequestBody MissionRequest req) {
        return adminService.createMission(req, TenantContext.getTenantId());
    }

    @PutMapping("/missions/{id}")
    public MissionResponse updateMission(@PathVariable UUID id,
                                         @Valid @RequestBody MissionRequest req) {
        return adminService.updateMission(id, req);
    }

    @DeleteMapping("/missions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateMission(@PathVariable UUID id) {
        adminService.deactivateMission(id);
    }
}