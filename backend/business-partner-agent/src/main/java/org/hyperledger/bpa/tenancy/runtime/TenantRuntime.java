package org.hyperledger.bpa.tenancy.runtime;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@ConfigurationProperties("tenancy")
public class TenantRuntime {

    private String jwtTenantAttribute;
    private String apiKey;
    private String defaultTenant;

    public String getJwtTenantAttribute() {
        return jwtTenantAttribute;
    }

    public void setJwtTenantAttribute(String jwtTenantAttribute) {
        this.jwtTenantAttribute = jwtTenantAttribute;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getDefaultTenant() {
        return defaultTenant;
    }

    public void setDefaultTenant(String defaultTenant) {
        this.defaultTenant = defaultTenant;
    }

}