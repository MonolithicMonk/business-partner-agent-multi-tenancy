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
}
