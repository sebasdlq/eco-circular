package com.ecocircular.ecocircular.iam.infrastructure.security;

import com.ecocircular.ecocircular.iam.application.JwtService;
import com.ecocircular.ecocircular.common.base.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                var claims = jwtService.parseToken(token);
                String email = claims.getSubject();
                UUID userId = UUID.fromString(claims.get("user_id", String.class));
                UUID tenantId = UUID.fromString(claims.get("tenant_id", String.class));
                List<String> roles = claims.get("roles", List.class);
                List<String> permissions = claims.get("permissions", List.class);

                TenantContext.setTenantId(tenantId);

                var authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                // También agregar permisos como authorities para @PreAuthorize
                permissions.stream()
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);

                var authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // Token inválido, no autenticar
            }
        }
        filterChain.doFilter(request, response);
        TenantContext.clear(); // limpiamos después de la petición
    }
}