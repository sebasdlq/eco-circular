package com.ecocircular.ecocircular.iam.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.UUID;

@Data
public class AssignRoleRequest {
    @NotBlank @Email
    private String email;

    @NotBlank
    private String role; // ej: "GREEN_POINT_OPERATOR"

    // Opcionales para crear un nuevo usuario
    private String displayName;
    private String password;
}