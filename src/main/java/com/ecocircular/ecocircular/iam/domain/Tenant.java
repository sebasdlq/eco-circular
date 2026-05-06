package com.ecocircular.ecocircular.iam.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@Getter @Setter @NoArgsConstructor
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String type;    // MUNICIPIO, ESCUELA, etc.
    @Column(columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private String config;  // JSON simple
    private boolean isActive = true;
    private LocalDateTime createdAt = LocalDateTime.now();
}
