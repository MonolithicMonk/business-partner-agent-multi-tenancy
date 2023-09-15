package org.hyperledger.bpa.constants;

import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

/**
 * Holds constants related to tenancy.
 */
@Singleton
public class TenancyConstants {

    /**
     * Default tenant value. If not set via configuration, it defaults to "internaloperations".
     */
    @Value("${tenancy.defaultTenant:internaloperations}")
    public String DEFAULT_TENANT;

    /**
     * Default walletId header name. If not set via configuration, it defaults to "x-wallet-id".
     */
    @Value("${tenancy.walletIdHeaderName:x-wallet-id}")
    public String WALLET_ID_HEADER_NAME;

    /**
     * Default acapy api key header name. If not set via configuration, it defaults to "x-api-key".
     */
    @Value("${tenancy.acapyAdminApiKeyHeaderName:x-api-key}")
    public String ACAPY_ADMIN_API_KEY_HEADER_NAME;
}
