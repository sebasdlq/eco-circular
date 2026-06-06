package com.ecocircular.ecocircular.recyclingops.application;

import com.ecocircular.ecocircular.common.audit.AuditContext;
import com.ecocircular.ecocircular.common.audit.AuditEvents;
import com.ecocircular.ecocircular.common.audit.AuditService;
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
    private final AuditService              auditService;

    // ------------------------------------------------------------------ //
    //  CRUD                                                                //
    // ------------------------------------------------------------------ //

    public List<DeliveryResponse> getAllByCurrentTenant() {
        UUID tenantId = TenantContext.getTenantId();
        return deliveryRepository.findByGreenPoint_TenantId(tenantId)
                .stream()
                .map(this::toPublicResponse)
                .collect(Collectors.toList());
    }
    public List<DeliveryResponse> obtenerPorUserId(UUID id) {// Obtén el ID del usuario autenticado
        List<Delivery> deliveries = deliveryRepository.findByUserId(id);
        return deliveries.stream()
                .map(this::toPublicResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeliveryResponse crear(DeliveryCreateRequest request) {
        UUID   tenantId  = TenantContext.getTenantId();
        UUID   actorId   = AuditContext.getActorId();
        String actorName = AuditContext.getActorName();
        String clientIp  = AuditContext.getClientIp();

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario no encontrado: " + request.getUserId()));

        GreenPoint greenPoint = greenPointRepository.findById(request.getGreenPointId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "GreenPoint no encontrado: " + request.getGreenPointId()));

        Delivery delivery = new Delivery();
        delivery.setUser(user);
        delivery.setGreenPoint(greenPoint);

        // FIX: guardar primero para tener ID, luego asignar los detalles con referencia al delivery
        Delivery saved = deliveryRepository.save(delivery);

        List<DeliveryDetail> details = toDetailEntities(request.getDetails(), saved);
        saved.getDetails().addAll(details);
        saved = deliveryRepository.save(saved);

        DeliveryResponse res = toPublicResponse(saved);
       /* auditService.registrar(
                "Delivery", saved.getId(), AuditEvents.ENTREGA_CREADA,
                null, res,
                tenantId, actorId, actorName, clientIp, null
        );*/

        return res;
    }

    @Transactional(readOnly = true)
    public DeliveryResponse obtenerPorId(UUID id) {
        return toPublicResponse(findOrThrow(id));
    }

    @Transactional
    public DeliveryResponse validar(UUID id) {
        UUID   tenantId  = TenantContext.getTenantId();
        UUID   actorId   = AuditContext.getActorId();
        String actorName = AuditContext.getActorName();
        String clientIp  = AuditContext.getClientIp();

        Delivery delivery = findOrThrow(id);
        DeliveryResponse estadoAnterior = toPublicResponse(delivery);

        delivery.validarEntrega();
        Delivery saved = deliveryRepository.save(delivery);

        auditService.registrar(
                "Delivery", saved.getId(), AuditEvents.ENTREGA_VALIDADA,
                estadoAnterior, toPublicResponse(saved),
                tenantId, actorId, actorName, clientIp, null
        );

        return toPublicResponse(saved);
    }

    @Transactional
    public DeliveryResponse ajustarCantidades(UUID id, List<DeliveryDetailRequest> newDetails) {
        UUID   tenantId  = TenantContext.getTenantId();
        UUID   actorId   = AuditContext.getActorId();
        String actorName = AuditContext.getActorName();
        String clientIp  = AuditContext.getClientIp();
        String reason    = AuditContext.getReason();

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Se requiere una razón para ajustar cantidades de una entrega.");
        }

        Delivery delivery = findOrThrow(id);
        DeliveryResponse estadoAnterior = toPublicResponse(delivery);

        delivery.ajustarCantidades(toDetailEntities(newDetails, delivery));
        Delivery saved = deliveryRepository.save(delivery);

        auditService.registrarSync(
                "Delivery", saved.getId(), AuditEvents.ENTREGA_AJUSTADA,
                estadoAnterior, toPublicResponse(saved),
                tenantId, actorId, actorName, clientIp, reason
        );

        return toPublicResponse(saved);
    }

    @Transactional
    public void eliminar(UUID id) {
        UUID   tenantId  = TenantContext.getTenantId();
        UUID   actorId   = AuditContext.getActorId();
        String actorName = AuditContext.getActorName();
        String clientIp  = AuditContext.getClientIp();

        Delivery delivery = findOrThrow(id);
        DeliveryResponse snapshot = toPublicResponse(delivery);

        deliveryRepository.deleteById(id);

        auditService.registrarSync(
                "Delivery", id, AuditEvents.ENTREGA_ELIMINADA,
                snapshot, null,
                tenantId, actorId, actorName, clientIp, null
        );
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //

    private Delivery findOrThrow(UUID id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Delivery no encontrada: " + id));
    }

    // FIX: recibe el Delivery padre para asignar la referencia en cada detail
    private List<DeliveryDetail> toDetailEntities(List<DeliveryDetailRequest> requests, Delivery delivery) {
        return requests.stream().map(req -> {
            MaterialCatalog material = materialCatalogRepository.findById(req.getMaterialId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Material no encontrado: " + req.getMaterialId()));

            DeliveryDetail detail = new DeliveryDetail();
            detail.setDelivery(delivery);           // FIX: asignar referencia al padre
            detail.setMaterial(material);
            detail.setQuantity(req.getQuantity());

            int points = (int) Math.round(req.getQuantity() * material.getPointsPerUnit());
            BigDecimal co2 = BigDecimal.valueOf(req.getQuantity() * material.getCo2Factor())
                    .setScale(4, RoundingMode.HALF_UP);

            detail.setPointsEarned(points);
            detail.setCo2Estimated(co2);
            return detail;
        }).collect(Collectors.toList());
    }

    public DeliveryResponse toPublicResponse(Delivery d) {
        DeliveryResponse res = new DeliveryResponse();
        res.setId(d.getId());
        res.setUserId(d.getUser().getId());
        res.setUserDisplayName(d.getUser().getDisplayName());
        res.setGreenPointId(d.getGreenPoint().getId());
        res.setGreenPointName(d.getGreenPoint().getName());
        res.setStatus(d.getStatus());
        res.setDeliveredAt(d.getDeliveredAt());
        res.setDetails(d.getDetails().stream().map(detail -> {
            DeliveryDetailResponse dr = new DeliveryDetailResponse();
            dr.setMaterialId(detail.getMaterial().getId());
            dr.setMaterialName(detail.getMaterial().getName());
            dr.setQuantity(detail.getQuantity());
            dr.setPointsEarned(detail.getPointsEarned());
            dr.setCo2Estimated(detail.getCo2Estimated());
            return dr;
        }).collect(Collectors.toList()));
        return res;
    }
}