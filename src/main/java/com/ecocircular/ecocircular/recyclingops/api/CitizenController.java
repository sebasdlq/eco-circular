package com.ecocircular.ecocircular.recyclingops.api;

import com.ecocircular.ecocircular.iam.application.JwtService;
import com.ecocircular.ecocircular.recyclingops.application.CitizenService;
import com.ecocircular.ecocircular.recyclingops.dto.*;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/citizens")
@RequiredArgsConstructor
public class CitizenController {

    private final CitizenService citizenService;
    private final JwtService     jwtService;

    // ── Historial personal ────────────────────────────────────────────────────
    @GetMapping("/my/history")
    @PreAuthorize("hasAuthority('VIEW_OWN_IMPACT') or hasAuthority('INIT_DELIVERY')")
    public ResponseEntity<List<DeliveryResponse>> myHistory(HttpServletRequest request) {
        UUID userId = extractUserId(request);
        return ResponseEntity.ok(citizenService.getMyHistory(userId));
    }

    // ── Impacto personal (kg, CO2, puntos, entregas) ──────────────────────────
    @GetMapping("/my/impact")
    @PreAuthorize("hasAuthority('VIEW_OWN_IMPACT') or hasAuthority('INIT_DELIVERY')")
    public ResponseEntity<CitizenImpactResponse> myImpact(HttpServletRequest request) {
        UUID userId = extractUserId(request);
        return ResponseEntity.ok(citizenService.getMyImpact(userId));
    }

    // ── Ranking del tenant ────────────────────────────────────────────────────
    @GetMapping("/ranking")
    @PreAuthorize("hasAuthority('VIEW_RANKING') or hasAuthority('VIEW_ALL_ANALYTICS')")
    public ResponseEntity<List<CitizenRankingEntry>> ranking() {
        return ResponseEntity.ok(citizenService.getRanking());
    }

    // ── Helper: extraer userId del JWT ────────────────────────────────────────
    private UUID extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Claims claims = jwtService.parseToken(token);
        return UUID.fromString(claims.get("user_id", String.class));
    }
}