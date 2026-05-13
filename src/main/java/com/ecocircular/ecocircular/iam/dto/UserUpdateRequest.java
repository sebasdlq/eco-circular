// com.ecocircular.ecocircular.iam.dto.UserUpdateRequest.java
package com.ecocircular.ecocircular.iam.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class UserUpdateRequest {
    private String email;
    private String displayName;
    private String password;      // si se envía, se actualiza
    private UUID activeTenantId;
}