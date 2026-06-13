package com.ecocircular.ecocircular.gamification.domain;

import com.ecocircular.ecocircular.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "missions")
@Getter @Setter @NoArgsConstructor
@AttributeOverride(name = "tenantId", column = @Column(name = "tenant_id", nullable = true))
public class Mission extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 300)
    private String description;

    /**
     * Tipo de métrica que mide esta misión.
     * Valores válidos: DELIVERIES | KG_RECYCLED | GREEN_POINT_VISITS | MATERIALS_VARIETY
     */
    @Column(name = "target_type", nullable = false, length = 50)
    private String targetType;

    @Column(name = "target_value", nullable = false)
    private double targetValue;

    @Column(name = "reward_points", nullable = false)
    private int rewardPoints;

    /** Null = sin vencimiento fijo; con valor = misión de campaña temporal */
    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(nullable = false)
    private boolean active = true;

    public Mission(String name, String description,
                   String targetType, double targetValue,
                   int rewardPoints, LocalDate deadline) {
        this.name         = name;
        this.description  = description;
        this.targetType   = targetType;
        this.targetValue  = targetValue;
        this.rewardPoints = rewardPoints;
        this.deadline     = deadline;
        this.active       = true;
    }
}
