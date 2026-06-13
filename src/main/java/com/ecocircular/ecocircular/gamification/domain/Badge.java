package com.ecocircular.ecocircular.gamification.domain;

import com.ecocircular.ecocircular.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "badges")
@Getter @Setter @NoArgsConstructor
@AttributeOverride(name = "tenantId", column = @Column(name = "tenant_id", nullable = true))
public class Badge extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 300)
    private String description;

    /** Código de ícono para el frontend (ej: "RECYCLE_1", "GOLD_LEVEL") */
    @Column(name = "icon_code", length = 50)
    private String iconCode;

    @Column(nullable = false)
    private boolean active = true;

    public Badge(String name, String description, String iconCode) {
        this.name        = name;
        this.description = description;
        this.iconCode    = iconCode;
        this.active      = true;
    }

    public Badge(UUID uuid, String primerReciclaje, String desc, String icon) {
        super();
    }
}
