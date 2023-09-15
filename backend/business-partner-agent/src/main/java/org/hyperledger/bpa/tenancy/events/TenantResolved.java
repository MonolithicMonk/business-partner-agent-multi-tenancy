package org.hyperledger.bpa.tenancy.events;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class TenantResolved {

    private final String tenantId;

    public TenantResolved(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }
}
