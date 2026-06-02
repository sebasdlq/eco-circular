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

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        UUID tenantId = TenantContext.getTenantId();
        List<Delivery> deliveries = deliveryRepository.findByGreenPoint_TenantId(tenantId);
        return deliveries.stream()
                .map(this::toPublicResponse)
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

        return toPublicResponse(deliveryRepository.save(delivery));
    }

    @Transactional(readOnly = true)
    public DeliveryResponse obtenerPorId(UUID id) {
        return toPublicResponse(findOrThrow(id));
    }

    @Transactional
    public DeliveryResponse validar(UUID id) {
        Delivery delivery = findOrThrow(id);
        delivery.validarEntrega();
        return toPublicResponse(deliveryRepository.save(delivery));
    }

    @Transactional
    public DeliveryResponse ajustarCantidades(UUID id, List<DeliveryDetailRequest> newDetails) {
        Delivery delivery = findOrThrow(id);
        delivery.ajustarCantidades(toDetailEntities(newDetails));
        return toPublicResponse(deliveryRepository.save(delivery));
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!deliveryRepository.existsById(id)) {
            throw new IllegalArgumentException("Delivery no encontrada: " + id);
        }
        deliveryRepository.deleteById(id);
    }

    // ------------------------------------------------------------------ //
    //  Helpers — público para que CitizenService pueda reutilizarlo        //
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

            // Calcular puntos y CO2 desde el catálogo
            int points = (int) Math.round(req.getQuantity() * material.getPointsPerUnit());
            BigDecimal co2 = BigDecimal.valueOf(req.getQuantity() * material.getCo2Factor())
                    .setScale(4, RoundingMode.HALF_UP);

            detail.setPointsEarned(points);
            detail.setCo2Estimated(co2);

            return detail;
        }).collect(Collectors.toList());
    }

    // ✅ público para reutilizar en CitizenService
    public DeliveryResponse toPublicResponse(Delivery d) {
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