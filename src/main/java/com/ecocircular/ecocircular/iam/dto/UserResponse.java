// com.ecocircular.ecocircular.iam.dto.UserResponse.java
package com.ecocircular.ecocircular.iam.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String email;
    private String displayName;
    private UUID activeTenantId;
    private LocalDateTime createdAt;
    // no se expone passwordHash ni deletedAt
}