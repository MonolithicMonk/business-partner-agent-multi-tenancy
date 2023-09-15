package org.hyperledger.bpa.tenancy.runtime;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@ConfigurationProperties("tenancy")
public class TenantRuntime {

    private String jwtTenantAttribute;
    private String apiKey;
    private String defaultTenant;
    private String walletIdHeaderName;
    private String acapyAdminApiKeyHeaderName;

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

    public String getWalletIdHeaderName() {
        return walletIdHeaderName;
    }

    public void setWalletIdHeaderName(String walletIdHeaderName) {
        this.walletIdHeaderName = walletIdHeaderName;
    }

    public String getAcapyAdminApiKeyHeaderName() {
        return acapyAdminApiKeyHeaderName;
    }

    public void setAcapyAdminApiKeyHeaderName(String acapyAdminApiKeyHeaderName) {
        this.acapyAdminApiKeyHeaderName = acapyAdminApiKeyHeaderName;
    }

}