package com.ecocircular.ecocircular.recyclingops.domain;

import com.ecocircular.ecocircular.common.base.BaseEntity;
import com.ecocircular.ecocircular.iam.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "deliveries")
@Getter @Setter
public class Delivery extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "green_point_id", nullable = false)
    private GreenPoint greenPoint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    // DeliveryDetail es un value object — se embebe en la misma tabla como JSONB
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "delivery_details", joinColumns = @JoinColumn(name = "delivery_id"))
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
        this.details = newDetails;
        this.status = DeliveryStatus.ADJUSTED;
    }
}