package com.ecocircular.ecocircular.iam.application;

import com.ecocircular.ecocircular.common.base.TenantContext;
import com.ecocircular.ecocircular.iam.domain.Tenant;
import com.ecocircular.ecocircular.iam.domain.User;
import com.ecocircular.ecocircular.iam.domain.UserTenantRole;
import com.ecocircular.ecocircular.iam.infrastructure.persistence.TenantRepository;
import com.ecocircular.ecocircular.iam.infrastructure.persistence.UserRepository;
import com.ecocircular.ecocircular.iam.infrastructure.persistence.UserTenantRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final UserTenantRoleRepository userTenantRoleRepository;



    @Transactional
    public Tenant create(Tenant tenant) {
        tenant.setActive(true);
        return tenantRepository.save(tenant);
    }

    public List<Tenant> listAll() {
        return tenantRepository.findAll();
    }

    public Tenant getById(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant no encontrado: " + id));
    }

    @Transactional
    public Tenant update(UUID id, Tenant incoming) {
        Tenant existing = getById(id);
        existing.setName(incoming.getName());
        existing.setType(incoming.getType());
        existing.setConfig(incoming.getConfig());
        return tenantRepository.save(existing);
    }

    @Transactional
    public void disable(UUID id) {
        Tenant tenant = getById(id);
        tenant.setActive(false);
        tenantRepository.save(tenant);
    }


    public List<UserTenantRole> listRolesInCurrentTenant() {
        UUID tenantId = TenantContext.getTenantId();
        return userTenantRoleRepository.findByTenantId(tenantId);
    }


    @Transactional
    public UserTenantRole assignRole(UUID userId, String role, UUID assignedBy) {
        UUID tenantId = TenantContext.getTenantId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userId));

        Tenant tenant = getById(tenantId);

        boolean alreadyAssigned = userTenantRoleRepository
                .findByTenantId(tenantId)
                .stream()
                .anyMatch(r -> r.getUser().getId().equals(userId) && r.getRole().equals(role));

        if (alreadyAssigned) {
            throw new RuntimeException("El usuario ya tiene el rol '" + role + "' en este tenant");
        }

        UserTenantRole utr = new UserTenantRole();
        utr.setUser(user);
        utr.setTenant(tenant);
        utr.setRole(role);
        utr.setAssignedBy(assignedBy);

        return userTenantRoleRepository.save(utr);
    }


    @Transactional
    public void revokeRole(UUID userTenantRoleId) {
        UUID tenantId = TenantContext.getTenantId();

        UserTenantRole utr = userTenantRoleRepository.findById(userTenantRoleId)
                .orElseThrow(() -> new RuntimeException("Asignación de rol no encontrada: " + userTenantRoleId));

        if (!utr.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("La asignación no pertenece al tenant actual");
        }

        userTenantRoleRepository.delete(utr);
    }
}