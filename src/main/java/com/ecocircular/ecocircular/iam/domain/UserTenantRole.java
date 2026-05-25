package com.ecocircular.ecocircular.iam.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_tenant_roles")
@Getter @Setter @NoArgsConstructor
public class UserTenantRole {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    private String role;          // ROLE_MUNICIPALITY_ADMIN, GREEN_POINT, MEMBER.
    private LocalDateTime assignedAt = LocalDateTime.now();
    private UUID assignedBy;      // usuario que asignó
}