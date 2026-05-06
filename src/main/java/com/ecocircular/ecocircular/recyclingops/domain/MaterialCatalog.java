package com.ecocircular.ecocircular.recyclingops.domain;

import com.ecocircular.ecocircular.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "material_catalog")
@Getter @Setter @NoArgsConstructor
public class MaterialCatalog extends BaseEntity {
    // tenant_id en BaseEntity, pero aquí puede ser NULL para global
    // Sobrescribimos para permitir NULL
    @Column(name = "tenant_id", nullable = true)
    private UUID tenantId;  // null = global

    private String name;
    private String category;
    private String unit;
    private int pointsPerUnit;
    @Column(name = "co2_factor")
    private double co2Factor;
    @Column(columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private String equivalents; // JSON con equivalencias
    private boolean isActive = true;
    private LocalDate effectiveFrom;
}
