package com.ecocircular.ecocircular.iam.api;

import com.ecocircular.ecocircular.iam.application.AuthService;
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
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        UserResponse user = userService.getUserByEmail(request.email);

        return ResponseEntity.ok(Map.of("token", token, "id", user.getId().toString(), "name", user.getDisplayName()));
    }

    record LoginRequest(String email, String password) {}
}