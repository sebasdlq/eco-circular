package com.ecocircular.ecocircular.iam.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String email;
    private String displayName;
    private String passwordHash;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne
    private Tenant activeTenant;

    private LocalDateTime createdAt = LocalDateTime.now();
}