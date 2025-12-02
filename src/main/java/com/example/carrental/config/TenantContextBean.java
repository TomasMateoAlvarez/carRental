package com.example.carrental.config;

import org.springframework.stereotype.Component;

@Component("tenantContext")
public class TenantContextBean {

    public Long getTenantId() {
        return TenantContext.getTenantId();
    }

    public void setTenantId(Long tenantId) {
        TenantContext.setTenantId(tenantId);
    }

    public boolean hasTenant() {
        return TenantContext.hasTenant();
    }
}