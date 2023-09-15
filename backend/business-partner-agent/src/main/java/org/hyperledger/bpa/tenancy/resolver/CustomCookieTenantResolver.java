/*
 * Copyright 2017-2023 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hyperledger.bpa.tenancy.resolver;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.multitenancy.exceptions.TenantNotFoundException;
import io.micronaut.multitenancy.tenantresolver.CookieTenantResolverConfiguration;
import io.micronaut.multitenancy.tenantresolver.CookieTenantResolverConfigurationProperties;
import io.micronaut.multitenancy.tenantresolver.TenantResolver;
import io.micronaut.multitenancy.tenantresolver.CookieTenantResolver;

import org.hyperledger.bpa.tenancy.events.TenantResolved;
import org.hyperledger.bpa.constants.TenancyConstants;

import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import io.micronaut.context.annotation.Replaces;
import jakarta.inject.Inject;

/**
 * A custom {@link TenantResolver} that resolves the tenant from a request cookie.
 * This resolver extends the default CookieTenantResolver to introduce custom behavior.
 * 
 * <p>
 * When a tenant is resolved, a TenantResolved event is emitted to notify other parts of the
 * application about the resolved tenant. This allows for dynamic configuration or behavior
 * based on the tenant context.
 * </p>
 * 
 * <p>
 * If the tenant cannot be resolved from the cookie, a default tenant ("internaloperations") 
 * is used, and the TenantResolved event is emitted with this default tenant.
 * </p>
 * 
 * @author Sergio del Amo
 * @since 1.0.0
 * @modified Yuki I - Returns a default Tenant when resolution fails.
 * @modified Yuki I - Added event emission on tenant resolution.
 */

@Slf4j
@Singleton
@Replaces(CookieTenantResolver.class)
@Requires(beans = CookieTenantResolverConfiguration.class)
@Requires(property = CookieTenantResolverConfigurationProperties.PREFIX + ".enabled", value = StringUtils.TRUE,
        defaultValue = StringUtils.FALSE)
public class CustomCookieTenantResolver extends CookieTenantResolver {
  
    @Inject
    ApplicationEventPublisher<TenantResolved> tenantResolvedEventPublisher;
    
    @Inject
    TenancyConstants tenancyConstants;
  
    public CustomCookieTenantResolver(CookieTenantResolverConfiguration configuration) {
        super(configuration);
    }

    @Override
    @NonNull
    public Serializable resolveTenantIdentifier() {
        try {
            Serializable tenantId = super.resolveTenantIdentifier();
            log.debug("Tenant resolved: {}", tenantId);
            emitTenantResolvedEvent(tenantId.toString());
            return tenantId;
        } catch (TenantNotFoundException e) {
            log.debug("Tenant not found, using default: {}", tenancyConstants.DEFAULT_TENANT);
            emitTenantResolvedEvent(tenancyConstants.DEFAULT_TENANT);
            return tenancyConstants.DEFAULT_TENANT;
        }
    }

    @Override
    @NonNull
    public Serializable resolveTenantIdentifier(@NonNull HttpRequest<?> request) {
        try {
            Serializable tenantId = super.resolveTenantIdentifier(request);
            log.debug("Tenant resolved: {}", tenantId);
            emitTenantResolvedEvent(tenantId.toString());
            return tenantId;
        } catch (TenantNotFoundException e) {
            log.debug("Tenant not found, using default: {}", tenancyConstants.DEFAULT_TENANT);
            emitTenantResolvedEvent(tenancyConstants.DEFAULT_TENANT);
            return tenancyConstants.DEFAULT_TENANT;
        }
    }

    /**
     * Emits a TenantResolved event with the provided tenantId.
     * 
     * @param tenantId The resolved tenant identifier.
     */
    private void emitTenantResolvedEvent(String tenantId) {
      tenantResolvedEventPublisher.publishEvent(new TenantResolved(tenantId));
    }
}
