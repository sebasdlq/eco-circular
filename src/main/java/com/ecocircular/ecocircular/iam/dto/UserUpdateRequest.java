// com.ecocircular.ecocircular.iam.dto.UserUpdateRequest.java
package com.ecocircular.ecocircular.iam.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;

@Getter @Setter
public class UserUpdateRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Display name is required")
    private String displayName;

    // Opcional: solo se cambia si viene en el request
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private UUID activeTenantId;
}