package com.ecocircular.ecocircular.iam.api;

import com.ecocircular.ecocircular.iam.application.TenantService;
import com.ecocircular.ecocircular.iam.domain.Tenant;
import com.ecocircular.ecocircular.iam.domain.UserTenantRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;



    @PostMapping
    @PreAuthorize("hasRole('ROLE_MUNICIPALITY_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Tenant create(@RequestBody Tenant tenant) {
        return tenantService.create(tenant);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_MUNICIPALITY_ADMIN')")
    public List<Tenant> listAll() {
        return tenantService.listAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_MUNICIPALITY_ADMIN')")
    public Tenant getById(@PathVariable UUID id) {
        return tenantService.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_MUNICIPALITY_ADMIN')")
    public Tenant update(@PathVariable UUID id, @RequestBody Tenant tenant) {
        return tenantService.update(id, tenant);
    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasRole('ROLE_MUNICIPALITY_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable UUID id) {
        tenantService.disable(id);
    }


    @GetMapping("/current/roles")
    @PreAuthorize("hasRole('ROLE_MUNICIPALITY_ADMIN')")
    public List<UserTenantRole> listRoles() {
        return tenantService.listRolesInCurrentTenant();
    }

    @PostMapping("/current/roles")
    @PreAuthorize("hasAuthority('MANAGE_ZONES')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserTenantRole assignRole(
            @RequestBody AssignRoleRequest request,
            @AuthenticationPrincipal String email
    ) {

        return tenantService.assignRole(request.userId(), request.role(), request.assignedBy());
    }

    @DeleteMapping("/current/roles/{userTenantRoleId}")
    @PreAuthorize("hasAuthority('MANAGE_ZONES')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeRole(@PathVariable UUID userTenantRoleId) {
        tenantService.revokeRole(userTenantRoleId);
    }



    record AssignRoleRequest(UUID userId, String role, UUID assignedBy) {}
}