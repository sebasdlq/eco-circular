package com.ecocircular.ecocircular.iam.api;

import com.ecocircular.ecocircular.iam.application.AuthService;
import com.ecocircular.ecocircular.iam.application.UserService;
import jakarta.servlet.http.HttpServletRequest;
import com.ecocircular.ecocircular.iam.application.UserService;
import com.ecocircular.ecocircular.iam.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {          // ← nuevo: para obtener la IP

        String clientIp = resolverIp(httpRequest);
        String token = authService.login(request.email(), request.password(), clientIp);
        UserResponse user = userService.getUserByEmail(request.email);

        return ResponseEntity.ok(Map.of("token", token, "id", user.getId().toString(), "name", user.getDisplayName()));
    }

    private String resolverIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @PostMapping("/register-tenant")
    public ResponseEntity<Map<String, String>> registerToTenant(
            @RequestBody RegisterTenantRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = resolverIp(httpRequest);
        Map<String, String> result = authService.registerToTenant(
                request.email(), request.password(), request.tenantId(), clientIp
        );
        return ResponseEntity.ok(result);
    }

    // DTO
    record RegisterTenantRequest(String email, String password, UUID tenantId) {}

    record LoginRequest(String email, String password) {}
}