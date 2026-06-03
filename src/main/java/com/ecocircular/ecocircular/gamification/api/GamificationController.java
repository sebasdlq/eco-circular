package com.ecocircular.ecocircular.gamification.api;

import com.ecocircular.ecocircular.common.base.TenantContext;
import com.ecocircular.ecocircular.gamification.api.dto.*;
import com.ecocircular.ecocircular.gamification.application.GamificationService;
import com.ecocircular.ecocircular.gamification.domain.RankingScope;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;

    @GetMapping("/users/{userId}")
    public UserGamificationResponse getUserSummary(@PathVariable UUID userId) {
        return UserGamificationResponse.from(
                gamificationService.getSummary(TenantContext.getTenantId(), userId));
    }

    @GetMapping("/users/{userId}/badges")
    public List<BadgeResponse> getUserBadges(@PathVariable UUID userId) {
        return gamificationService.getBadges(TenantContext.getTenantId(), userId)
                .stream().map(BadgeResponse::from).toList();
    }

    @GetMapping("/users/{userId}/missions")
    public List<MissionResponse> getUserMissions(@PathVariable UUID userId) {
        return gamificationService.getMissions(TenantContext.getTenantId(), userId)
                .stream().map(MissionResponse::from).toList();
    }

    @GetMapping("/users/{userId}/recommendation")
    public List<RecommendationResponse> getUserRecommendations(@PathVariable UUID userId) {
        return gamificationService.getRecommendations(TenantContext.getTenantId(), userId)
                .stream().map(RecommendationResponse::from).toList();
    }

    @GetMapping("/ranking")
    public RankingResponse getRanking(
            @RequestParam(defaultValue = "GLOBAL") String scope) {
        RankingScope rankingScope;
        try {
            rankingScope = RankingScope.valueOf(scope.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Scope inválido: '" + scope + "'. Valores válidos: GLOBAL, SCHOOL, ZONE");
        }
        return RankingResponse.from(
                rankingScope,
                gamificationService.getRanking(TenantContext.getTenantId(), rankingScope));
    }
}
