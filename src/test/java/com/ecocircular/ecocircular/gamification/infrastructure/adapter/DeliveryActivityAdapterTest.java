package com.ecocircular.ecocircular.gamification.infrastructure.adapter;

import com.ecocircular.ecocircular.gamification.application.port.UserRecyclingActivity;
import com.ecocircular.ecocircular.iam.domain.Tenant;
import com.ecocircular.ecocircular.iam.domain.User;
import com.ecocircular.ecocircular.iam.domain.UserTenantRole;
import com.ecocircular.ecocircular.iam.infrastructure.persistence.UserRepository;
import com.ecocircular.ecocircular.iam.infrastructure.persistence.UserTenantRoleRepository;
import com.ecocircular.ecocircular.recyclingops.domain.*;
import com.ecocircular.ecocircular.recyclingops.infrastructure.persistence.DeliveryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryActivityAdapterTest {

    @Mock DeliveryRepository deliveryRepository;
    @Mock UserRepository userRepository;
    @Mock UserTenantRoleRepository userTenantRoleRepository;

    @InjectMocks DeliveryActivityAdapter adapter;

    private static final UUID TENANT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID USER_ID   = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID GP_ID_1   = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID GP_ID_2   = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

    private User user;
    private GreenPoint gp1;
    private GreenPoint gp2;
    private MaterialCatalog plastic;
    private MaterialCatalog paper;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setDisplayName("Test User");

        gp1 = new GreenPoint();
        gp1.setId(GP_ID_1);
        gp1.setTenantId(TENANT_ID);

        gp2 = new GreenPoint();
        gp2.setId(GP_ID_2);
        gp2.setTenantId(TENANT_ID);

        plastic = new MaterialCatalog();
        plastic.setId(UUID.randomUUID());
        plastic.setName("Plástico PET");

        paper = new MaterialCatalog();
        paper.setId(UUID.randomUUID());
        paper.setName("Papel y Cartón");
    }

    // ── getUserRecyclingActivity ────────────────────────────────────────────

    @Test
    void getUserRecyclingActivity_withDeliveries_aggregatesCorrectly() {
        Delivery d1 = buildDelivery(GP_ID_1, gp1);
        DeliveryDetail det1 = buildDetail(d1, plastic, 10.0, 50, new BigDecimal("3.5000"));
        DeliveryDetail det2 = buildDetail(d1, paper,   5.0,  20, new BigDecimal("1.2000"));
        d1.getDetails().addAll(List.of(det1, det2));

        Delivery d2 = buildDelivery(GP_ID_2, gp2);
        DeliveryDetail det3 = buildDetail(d2, plastic, 8.0, 40, new BigDecimal("2.8000"));
        d2.getDetails().add(det3);

        when(deliveryRepository.findByUser_IdAndGreenPoint_TenantIdAndStatusNot(USER_ID, TENANT_ID, DeliveryStatus.DRAFT))
                .thenReturn(List.of(d1, d2));
        when(userTenantRoleRepository.findByUserId(USER_ID))
                .thenReturn(List.of(buildRole(USER_ID, TENANT_ID, "CIUDADANO")));

        UserRecyclingActivity result = adapter.getUserRecyclingActivity(TENANT_ID, USER_ID);

        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getDisplayName()).isEqualTo("Test User");
        assertThat(result.getRole()).isEqualTo("CIUDADANO");
        assertThat(result.getTotalDeliveries()).isEqualTo(2);
        assertThat(result.getTotalKgRecycled()).isEqualTo(23.0);
        assertThat(result.getTotalPoints()).isEqualTo(110.0);
        assertThat(result.getTotalCo2AvoidedKg()).isEqualTo(7.5);
        assertThat(result.getGreenPointVisits()).isEqualTo(2);
        assertThat(result.getMaterialsRecycled()).containsEntry("Plástico PET", 18.0);
        assertThat(result.getMaterialsRecycled()).containsEntry("Papel y Cartón", 5.0);
        assertThat(result.getLastDeliveryDate()).isNotNull();
    }

    @Test
    void getUserRecyclingActivity_noDeliveries_returnsZeroActivity() {
        when(deliveryRepository.findByUser_IdAndGreenPoint_TenantIdAndStatusNot(USER_ID, TENANT_ID, DeliveryStatus.DRAFT))
                .thenReturn(List.of());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userTenantRoleRepository.findByUserId(USER_ID)).thenReturn(List.of());

        UserRecyclingActivity result = adapter.getUserRecyclingActivity(TENANT_ID, USER_ID);

        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getDisplayName()).isEqualTo("Test User");
        assertThat(result.getTotalDeliveries()).isZero();
        assertThat(result.getTotalPoints()).isZero();
        assertThat(result.getTotalKgRecycled()).isZero();
        assertThat(result.getTotalCo2AvoidedKg()).isZero();
        assertThat(result.getGreenPointVisits()).isZero();
        assertThat(result.getMaterialsRecycled()).isEmpty();
        assertThat(result.getLastDeliveryDate()).isNull();
    }

    @Test
    void getUserRecyclingActivity_unknownUser_throwsIllegalArgumentException() {
        when(deliveryRepository.findByUser_IdAndGreenPoint_TenantIdAndStatusNot(USER_ID, TENANT_ID, DeliveryStatus.DRAFT))
                .thenReturn(List.of());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.getUserRecyclingActivity(TENANT_ID, USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(USER_ID.toString());
    }

    @Test
    void getUserRecyclingActivity_roleNotInTenant_defaultsToMember() {
        Delivery d = buildDelivery(GP_ID_1, gp1);
        d.getDetails().add(buildDetail(d, plastic, 5.0, 25, null));
        when(deliveryRepository.findByUser_IdAndGreenPoint_TenantIdAndStatusNot(USER_ID, TENANT_ID, DeliveryStatus.DRAFT))
                .thenReturn(List.of(d));
        when(userTenantRoleRepository.findByUserId(USER_ID)).thenReturn(List.of());

        UserRecyclingActivity result = adapter.getUserRecyclingActivity(TENANT_ID, USER_ID);

        assertThat(result.getRole()).isEqualTo("MEMBER");
    }

    @Test
    void getUserRecyclingActivity_singleGreenPoint_countsOneVisit() {
        Delivery d1 = buildDelivery(GP_ID_1, gp1);
        d1.getDetails().add(buildDetail(d1, plastic, 3.0, 15, null));
        Delivery d2 = buildDelivery(GP_ID_1, gp1);
        d2.getDetails().add(buildDetail(d2, paper, 2.0, 10, null));

        when(deliveryRepository.findByUser_IdAndGreenPoint_TenantIdAndStatusNot(USER_ID, TENANT_ID, DeliveryStatus.DRAFT))
                .thenReturn(List.of(d1, d2));
        when(userTenantRoleRepository.findByUserId(USER_ID)).thenReturn(List.of());

        UserRecyclingActivity result = adapter.getUserRecyclingActivity(TENANT_ID, USER_ID);

        assertThat(result.getTotalDeliveries()).isEqualTo(2);
        assertThat(result.getGreenPointVisits()).isEqualTo(1);
    }

    // ── getAllUsersRecyclingActivity ────────────────────────────────────────

    @Test
    void getAllUsersRecyclingActivity_groupsByUser() {
        UUID user2Id = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
        User user2 = new User();
        user2.setId(user2Id);
        user2.setDisplayName("User Two");

        Delivery d1 = buildDeliveryForUser(user,  gp1);
        d1.getDetails().add(buildDetail(d1, plastic, 10.0, 50, null));

        Delivery d2 = buildDeliveryForUser(user2, gp1);
        d2.getDetails().add(buildDetail(d2, paper, 5.0, 25, null));

        when(deliveryRepository.findByGreenPoint_TenantId(TENANT_ID)).thenReturn(List.of(d1, d2));
        when(userTenantRoleRepository.findByTenantIdWithUser(TENANT_ID)).thenReturn(List.of());

        List<UserRecyclingActivity> results = adapter.getAllUsersRecyclingActivity(TENANT_ID);

        assertThat(results).hasSize(2);
        UserRecyclingActivity act1 = results.stream().filter(a -> a.getUserId().equals(USER_ID)).findFirst().orElseThrow();
        assertThat(act1.getTotalPoints()).isEqualTo(50.0);
        assertThat(act1.getTotalKgRecycled()).isEqualTo(10.0);

        UserRecyclingActivity act2 = results.stream().filter(a -> a.getUserId().equals(user2Id)).findFirst().orElseThrow();
        assertThat(act2.getTotalPoints()).isEqualTo(25.0);
    }

    @Test
    void getAllUsersRecyclingActivity_excludesDraftDeliveries() {
        Delivery draft = buildDeliveryForUser(user, gp1);
        draft.setStatus(DeliveryStatus.DRAFT);
        draft.getDetails().add(buildDetail(draft, plastic, 10.0, 50, null));

        Delivery validated = buildDeliveryForUser(user, gp1);
        validated.getDetails().add(buildDetail(validated, plastic, 5.0, 25, null));

        when(deliveryRepository.findByGreenPoint_TenantId(TENANT_ID)).thenReturn(List.of(draft, validated));
        when(userTenantRoleRepository.findByTenantIdWithUser(TENANT_ID)).thenReturn(List.of());

        List<UserRecyclingActivity> results = adapter.getAllUsersRecyclingActivity(TENANT_ID);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTotalPoints()).isEqualTo(25.0);
    }

    @Test
    void getAllUsersRecyclingActivity_emptyTenant_returnsEmptyList() {
        when(deliveryRepository.findByGreenPoint_TenantId(TENANT_ID)).thenReturn(List.of());
        when(userTenantRoleRepository.findByTenantIdWithUser(TENANT_ID)).thenReturn(List.of());

        assertThat(adapter.getAllUsersRecyclingActivity(TENANT_ID)).isEmpty();
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private Delivery buildDelivery(UUID gpId, GreenPoint gp) {
        Delivery d = new Delivery();
        d.setId(UUID.randomUUID());
        d.setUser(user);
        d.setGreenPoint(gp);
        d.setStatus(DeliveryStatus.VALIDATED);
        d.setDeliveredAt(ZonedDateTime.now());
        return d;
    }

    private Delivery buildDeliveryForUser(User u, GreenPoint gp) {
        Delivery d = new Delivery();
        d.setId(UUID.randomUUID());
        d.setUser(u);
        d.setGreenPoint(gp);
        d.setStatus(DeliveryStatus.VALIDATED);
        d.setDeliveredAt(ZonedDateTime.now());
        return d;
    }

    private DeliveryDetail buildDetail(Delivery d, MaterialCatalog mat, double qty, int pts, BigDecimal co2) {
        DeliveryDetail detail = new DeliveryDetail();
        detail.setDelivery(d);
        detail.setMaterial(mat);
        detail.setQuantity(qty);
        detail.setPointsEarned(pts);
        detail.setCo2Estimated(co2);
        return detail;
    }

    private UserTenantRole buildRole(UUID userId, UUID tenantId, String role) {
        Tenant t = new Tenant();
        t.setId(tenantId);

        User u = new User();
        u.setId(userId);

        UserTenantRole utr = new UserTenantRole();
        utr.setUser(u);
        utr.setTenant(t);
        utr.setRole(role);
        return utr;
    }
}
