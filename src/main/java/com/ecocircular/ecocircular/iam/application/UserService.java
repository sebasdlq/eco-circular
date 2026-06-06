// com.ecocircular.ecocircular.iam.application.UserService.java
package com.ecocircular.ecocircular.iam.application;

import com.ecocircular.ecocircular.iam.domain.Tenant;
import com.ecocircular.ecocircular.iam.domain.User;
import com.ecocircular.ecocircular.iam.dto.UserCreateRequest;
import com.ecocircular.ecocircular.iam.dto.UserResponse;
import com.ecocircular.ecocircular.iam.dto.UserUpdateRequest;
import com.ecocircular.ecocircular.iam.infrastructure.persistence.TenantRepository;
import com.ecocircular.ecocircular.iam.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private final TenantRepository tenantRepository;
    @Autowired
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        // validar que el email no esté en uso (incluyendo eliminados lógicos)
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setDisplayName(request.getDisplayName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        // Resolver el Tenant desde el UUID del request
        if (request.getActiveTenantId() != null) {
            Tenant tenant = tenantRepository.findById(request.getActiveTenantId())
                    .orElseThrow(() -> new RuntimeException("Tenant not found"));
            user.setActiveTenant(tenant);
        }
        user.setCreatedAt(LocalDateTime.now());
        // deletedAt = null por defecto

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAllByDeletedAtIsNull(pageable)
                .map(this::toResponse);
    }

    @Transactional
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // validar email único si cambia
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        user.setEmail(request.getEmail());
        user.setDisplayName(request.getDisplayName());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getActiveTenantId() != null) {
            Tenant tenant = tenantRepository.findById(request.getActiveTenantId())
                    .orElseThrow(() -> new RuntimeException("Tenant not found"));
            user.setActiveTenant(tenant);
        }

        User updated = userRepository.save(user);
        return toResponse(updated);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setDisplayName(user.getDisplayName());
        response.setCreatedAt(user.getCreatedAt());

        if (user.getActiveTenant() != null) {
            response.setActiveTenantId(user.getActiveTenant().getId());
            response.setActiveTenantName(user.getActiveTenant().getName());
        }
        return response;
    }
}