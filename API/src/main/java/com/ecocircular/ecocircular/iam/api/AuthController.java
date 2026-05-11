package com.ecocircular.ecocircular.iam.api;

import com.ecocircular.ecocircular.iam.application.AuthService;
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

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password(), request.tenantId());
        return ResponseEntity.ok(Map.of("token", token));
    }

    record LoginRequest(String email, String password, UUID tenantId) {}
}