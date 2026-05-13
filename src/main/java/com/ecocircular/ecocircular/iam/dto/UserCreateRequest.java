// com.ecocircular.ecocircular.iam.dto.UserCreateRequest.java
package com.ecocircular.ecocircular.iam.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class UserCreateRequest {
    private String email;
    private String displayName;
    private String password;
    private UUID activeTenantId;  // opcional
}