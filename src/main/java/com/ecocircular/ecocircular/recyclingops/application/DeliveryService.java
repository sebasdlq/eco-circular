package com.ecocircular.ecocircular.recyclingops.application;

import com.ecocircular.ecocircular.common.base.TenantContext;
import com.ecocircular.ecocircular.iam.domain.User;
import com.ecocircular.ecocircular.iam.infrastructure.persistence.UserRepository;
import com.ecocircular.ecocircular.recyclingops.domain.*;
import com.ecocircular.ecocircular.recyclingops.dto.*;
import com.ecocircular.ecocircular.recyclingops.infrastructure.persistence.DeliveryRepository;
import com.ecocircular.ecocircular.recyclingops.infrastructure.persistence.GreenPointRepository;
import com.ecocircular.ecocircular.recyclingops.infrastructure.persistence.MaterialCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository        deliveryRepository;
    private final UserRepository            userRepository;
    private final GreenPointRepository      greenPointRepository;
    private final MaterialCatalogRepository materialCatalogRepository;

    // ------------------------------------------------------------------ //
    //  CRUD                                                                //
    // ------------------------------------------------------------------ //

    public List<DeliveryResponse> getAllByCurrentTenant() {
        // Obtener el tenant del contexto de seguridad
        UUID tenantId = TenantContext.getTenantId(); // ajusta según tu implementación
        List<Delivery> deliveries = deliveryRepository.findByGreenPoint_TenantId(tenantId);
        return deliveries.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    public List<DeliveryResponse> obtenerPorUserId(UUID id) {// Obtén el ID del usuario autenticado
        List<Delivery> deliveries = deliveryRepository.findByUserId(id);
        return deliveries.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeliveryResponse crear(DeliveryCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + request.getUserId()));

        GreenPoint greenPoint = greenPointRepository.findById(request.getGreenPointId())
                .orElseThrow(() -> new IllegalArgumentException("GreenPoint no encontrado: " + request.getGreenPointId()));

        Delivery delivery = new Delivery();
        delivery.setUser(user);
        delivery.setGreenPoint(greenPoint);
        delivery.setDetails(toDetailEntities(request.getDetails()));

        return toResponse(deliveryRepository.save(delivery));
    }

    @Transactional(readOnly = true)
    public DeliveryResponse obtenerPorId(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public DeliveryResponse validar(UUID id) {
        Delivery delivery = findOrThrow(id);
        delivery.validarEntrega();
        return toResponse(deliveryRepository.save(delivery));
    }

    @Transactional
    public DeliveryResponse ajustarCantidades(UUID id, List<DeliveryDetailRequest> newDetails) {
        Delivery delivery = findOrThrow(id);
        delivery.ajustarCantidades(toDetailEntities(newDetails));
        return toResponse(deliveryRepository.save(delivery));
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!deliveryRepository.existsById(id)) {
            throw new IllegalArgumentException("Delivery no encontrada: " + id);
        }
        deliveryRepository.deleteById(id);
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //

    private Delivery findOrThrow(UUID id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Delivery no encontrada: " + id));
    }

    private List<DeliveryDetail> toDetailEntities(List<DeliveryDetailRequest> requests) {
        return requests.stream().map(req -> {
            MaterialCatalog material = materialCatalogRepository.findById(req.getMaterialId())
                    .orElseThrow(() -> new IllegalArgumentException("Material no encontrado: " + req.getMaterialId()));

            DeliveryDetail detail = new DeliveryDetail();
            detail.setMaterial(material);
            detail.setQuantity(req.getQuantity());
            detail.setPointsEarned(req.getPointsEarned());
            detail.setCo2Estimated(req.getCo2Estimated());
            return detail;
        }).collect(Collectors.toList());
    }

    private DeliveryResponse toResponse(Delivery d) {
        DeliveryResponse res = new DeliveryResponse();
        res.setId(d.getId());
        res.setUserId(d.getUser().getId());
        res.setUserDisplayName(d.getUser().getDisplayName());
        res.setGreenPointId(d.getGreenPoint().getId());
        res.setGreenPointName(d.getGreenPoint().getName());
        res.setStatus(d.getStatus());
        res.setDeliveredAt(d.getDeliveredAt());

        List<DeliveryDetailResponse> detailResponses = d.getDetails().stream()
                .map(detail -> {
                    DeliveryDetailResponse dr = new DeliveryDetailResponse();
                    dr.setMaterialId(detail.getMaterial().getId());
                    dr.setMaterialName(detail.getMaterial().getName());
                    dr.setQuantity(detail.getQuantity());
                    dr.setPointsEarned(detail.getPointsEarned());
                    dr.setCo2Estimated(detail.getCo2Estimated());
                    return dr;
                }).collect(Collectors.toList());

        res.setDetails(detailResponses);
        return res;
    }
}