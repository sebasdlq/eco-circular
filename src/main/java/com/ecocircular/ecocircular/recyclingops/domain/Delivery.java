package com.ecocircular.ecocircular.recyclingops.domain;

import com.ecocircular.ecocircular.common.base.BaseEntity;
import com.ecocircular.ecocircular.iam.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "deliveries")
@Getter @Setter
public class Delivery extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "green_point_id", nullable = false)
    private GreenPoint greenPoint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<DeliveryDetail> details = new ArrayList<>();

    private ZonedDateTime deliveredAt;

    public Delivery() {
        this.status = DeliveryStatus.DRAFT;
        this.deliveredAt = ZonedDateTime.now();
    }

    public void validarEntrega() {
        if (status != DeliveryStatus.DRAFT) {
            throw new IllegalStateException("Delivery must be in DRAFT to validate");
        }
        if (details == null || details.isEmpty()) {
            throw new IllegalStateException("Delivery must have at least one detail");
        }
        this.status = DeliveryStatus.VALIDATED;
    }

    public void ajustarCantidades(List<DeliveryDetail> newDetails) {
        if (status != DeliveryStatus.VALIDATED) {
            throw new IllegalStateException("Delivery must be VALIDATED to adjust");
        }
        // Limpiar los detalles actuales y agregar los nuevos manteniendo la referencia al delivery
        this.details.clear();
        newDetails.forEach(d -> d.setDelivery(this));
        this.details.addAll(newDetails);
        this.status = DeliveryStatus.ADJUSTED;
    }
}