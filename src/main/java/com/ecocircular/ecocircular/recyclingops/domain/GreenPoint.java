package com.ecocircular.ecocircular.recyclingops.domain;

import com.ecocircular.ecocircular.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "green_points")
@Getter @Setter @NoArgsConstructor
public class GreenPoint extends BaseEntity {

    private String name;

    private Double locationLat;

    private Double locationLng;

    @Column(columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private String schedule;

    @Column(columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private String capacity;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "green_point_materials",
            joinColumns = @JoinColumn(name = "green_point_id"),
            inverseJoinColumns = @JoinColumn(name = "material_id")
    )
    private List<MaterialCatalog> acceptedMaterials = new ArrayList<>();

    private String status;
}