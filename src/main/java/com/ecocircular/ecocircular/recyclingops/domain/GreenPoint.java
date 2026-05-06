package com.ecocircular.ecocircular.recyclingops.domain;

import com.ecocircular.ecocircular.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "green_points")
@Getter @Setter @NoArgsConstructor
public class GreenPoint extends BaseEntity {
    private String name;
    private Double locationLat;
    private Double locationLng;
    @Column(columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private String schedule;    // JSON
    @Column(columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private String capacity;    // JSON
    @Column(columnDefinition = "uuid[]")
    private List<UUID> acceptedMaterials;
    private String status; // ACTIVE, INACTIVE, etc.
}