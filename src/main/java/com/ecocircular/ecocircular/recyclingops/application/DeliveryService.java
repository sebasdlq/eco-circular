package com.ecocircular.ecocircular.recyclingops.application;

import com.ecocircular.ecocircular.recyclingops.domain.Delivery;
import com.ecocircular.ecocircular.recyclingops.domain.DeliveryDetail;
import com.ecocircular.ecocircular.recyclingops.infrastructure.persistence.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    @Transactional
    public Delivery crear(Delivery delivery) {
        return deliveryRepository.save(delivery);
    }

    public Delivery obtenerPorId(UUID id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Delivery no encontrada: " + id));
    }

    @Transactional
    public Delivery validar(UUID id) {
        Delivery delivery = obtenerPorId(id);
        delivery.validarEntrega();
        return deliveryRepository.save(delivery);
    }

    @Transactional
    public Delivery ajustarCantidades(UUID id, List<DeliveryDetail> newDetails) {
        Delivery delivery = obtenerPorId(id);
        delivery.ajustarCantidades(newDetails);
        return deliveryRepository.save(delivery);
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!deliveryRepository.existsById(id)) {
            throw new IllegalArgumentException("Delivery no encontrada: " + id);
        }
        deliveryRepository.deleteById(id);
    }
}