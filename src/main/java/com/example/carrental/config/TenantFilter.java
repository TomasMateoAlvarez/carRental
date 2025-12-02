package com.example.carrental.config;

import com.example.carrental.services.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@Order(1)
public class TenantFilter implements Filter {

    @Autowired
    private JwtService jwtService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            // Extract tenant from JWT token
            String tenantId = extractTenantFromRequest(httpRequest);

            if (tenantId != null) {
                TenantContext.setTenantId(Long.valueOf(tenantId));
                log.debug("Set tenant context to: {}", tenantId);
            } else {
                // Default to organization 1 for backwards compatibility
                TenantContext.setDefaultTenant();
                log.debug("Using default tenant: 1");
            }

            chain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Error in tenant filter", e);
            // Set default tenant on error
            TenantContext.setDefaultTenant();
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String extractTenantFromRequest(HttpServletRequest request) {
        try {
            // First try to get from Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // Try to extract organizationId claim
                Object orgId = jwtService.extractClaim(token, claims -> claims.get("organizationId"));
                if (orgId != null) {
                    return orgId.toString();
                }
            }

            // Fallback: try to get from X-Tenant-ID header
            String tenantHeader = request.getHeader("X-Tenant-ID");
            if (tenantHeader != null) {
                return tenantHeader;
            }

            // Fallback: try to get from request parameter
            String tenantParam = request.getParameter("tenantId");
            if (tenantParam != null) {
                return tenantParam;
            }

        } catch (Exception e) {
            log.warn("Could not extract tenant from request", e);
        }

        return null; // Will default to tenant 1
    }
}