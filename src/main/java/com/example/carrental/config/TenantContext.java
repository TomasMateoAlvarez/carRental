package com.example.carrental.config;

public class TenantContext {

    private static final ThreadLocal<Long> CONTEXT = new ThreadLocal<>();

    public static void setTenantId(Long tenantId) {
        CONTEXT.set(tenantId);
    }

    public static Long getTenantId() {
        Long tenantId = CONTEXT.get();
        if (tenantId == null) {
            // Default to organization ID 1 for backwards compatibility
            return 1L;
        }
        return tenantId;
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static boolean hasTenant() {
        return CONTEXT.get() != null;
    }

    public static void setDefaultTenant() {
        setTenantId(1L);
    }
}