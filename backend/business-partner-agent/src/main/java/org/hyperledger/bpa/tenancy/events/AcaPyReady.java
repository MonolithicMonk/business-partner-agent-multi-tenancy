package org.hyperledger.bpa.tenancy.events;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class AcaPyReady {

    private final String tenantId;

    public AcaPyReady(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }
}
