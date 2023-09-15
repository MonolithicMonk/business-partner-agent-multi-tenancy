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

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.multitenancy.exceptions.TenantNotFoundException;
import io.micronaut.multitenancy.tenantresolver.CookieTenantResolverConfiguration;
import io.micronaut.multitenancy.tenantresolver.CookieTenantResolverConfigurationProperties;
import io.micronaut.multitenancy.tenantresolver.TenantResolver;
import io.micronaut.multitenancy.tenantresolver.CookieTenantResolver;

import org.hyperledger.bpa.tenancy.acapy.persistence.AcapyTenantConfigOperations;
import org.hyperledger.bpa.tenancy.controller.AriesWebhookController;
import org.hyperledger.bpa.constants.TenancyConstants;

import jakarta.inject.Singleton;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import java.io.Serializable;
import java.util.Optional;

/**
 * A {@link TenantResolver} that resolves the tenant from a request cookie.
 *
 * @author Yuki I
 * @since 1.0.0
 */

@Slf4j
@Singleton
@Replaces(CookieTenantResolver.class)
@Requires(beans = CookieTenantResolverConfiguration.class)
@Requires(property = CookieTenantResolverConfigurationProperties.PREFIX
    + ".enabled", value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
public class CustomCookieTenantResolver extends CookieTenantResolver {

  @Inject
  TenancyConstants tenancyConstants;

  private final AcapyTenantConfigOperations tenantConfigOps;

  public CustomCookieTenantResolver(CookieTenantResolverConfiguration configuration,
      AcapyTenantConfigOperations tenantConfigOps) {
    super(configuration);
    this.tenantConfigOps = tenantConfigOps;
  }

  @Override
  @NonNull
  public Serializable resolveTenantIdentifier() {
    try {
      Serializable tenantId = super.resolveTenantIdentifier();
      log.debug("Non HTTP Request tenant resolved: {}", tenantId.toString());
      return tenantId;
    } catch (TenantNotFoundException e) {
      log.debug("Non HTTP Request tenant not found, using default: {}", tenancyConstants.DEFAULT_TENANT);
      return tenancyConstants.DEFAULT_TENANT;
    }
  }

  /**
   * Resolves the tenant identifier from an HTTP request.
   * 
   * The method first attempts to resolve the tenant ID from db using the
   * 'x-wallet-id' header
   * if the request meets certain predefined conditions. If this attempt fails or
   * the conditions are not met, it falls back to the super class method to
   * resolve
   * the tenant ID.
   * 
   * @param request The current HTTP request
   * @return The resolved tenant identifier. Returns the DEFAULT_TENANT if no
   *         tenant
   *         could be resolved.
   * @throws TenantNotFoundException If the tenant could not be resolved.
   */
  @Override
  @NonNull
  public Serializable resolveTenantIdentifier(@NonNull HttpRequest<?> request) {
    try {
      Serializable tenantId = super.resolveTenantIdentifier(request);
      String ipAddress = request.getRemoteAddress().getAddress().toString();

      log.debug(
          "Request Headers: {}, IP: {}, Method: {}, URI: {}",
          request.getHeaders().asMap(), ipAddress,
          request.getMethod(), request.getUri());

      // Check if the request is a POST request and starts with the specified path
      // Since the only request that will contain the x-wallet-id is the request from
      // the acapy via events webhooks
      if (HttpMethod.POST.equals(request.getMethod())
          && request.getPath().startsWith(AriesWebhookController.WEBHOOK_CONTROLLER_PATH)) {

        // Extract the 'x-wallet-id' header from the request
        String walletId = request.getHeaders().get(tenancyConstants.WALLET_ID_HEADER_NAME);

        // If the 'x-wallet-id' is present, attempt to resolve the tenantId from db
        // using it
        if (walletId != null && !walletId.isEmpty()) {
          Optional<String> tenantIdInDb = tenantConfigOps.findTenantIdByWalletId(walletId);

          // If tenantId is successfully resolved using the 'x-wallet-id', return it
          // immediately
          if (tenantIdInDb.isPresent()) {
            log.debug("Tenant - {}, resolved using {} header", tenantIdInDb.get(), tenancyConstants.WALLET_ID_HEADER_NAME);
            return tenantIdInDb.get();
          }
        }
      }

      log.debug("HTTP Request tenant resolved: {}", tenantId);
      return tenantId;
    } catch (TenantNotFoundException e) {
      log.debug("HTTP Request tenant not found, using default: {}", tenancyConstants.DEFAULT_TENANT);
      return tenancyConstants.DEFAULT_TENANT;
    }
  }
}
