package com.ecocircular.ecocircular.recyclingops.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "batches")
@Getter @Setter
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID greenPointId;      // Referencia a GreenPoint (por ID)

    @Enumerated(EnumType.STRING)
    private BatchStatus status;

    // En lugar de @OneToMany con join, guardamos los IDs de las Deliveries
    @Column(columnDefinition = "uuid[]")
    private List<UUID> deliveryIds = new ArrayList<>();

    public Batch() {
        this.status = BatchStatus.OPEN;
    }

    public void addDelivery(UUID deliveryId) {
        this.deliveryIds.add(deliveryId);
    }

    public void dispatch() {
        if (status != BatchStatus.CLOSED) {
            throw new IllegalStateException("Batch must be CLOSED before dispatching");
        }
        this.status = BatchStatus.DISPATCHED;
    }

    public void receive() {
        if (status != BatchStatus.DISPATCHED) {
            throw new IllegalStateException("Batch must be DISPATCHED before receiving");
        }
        this.status = BatchStatus.RECEIVED;
    }
}