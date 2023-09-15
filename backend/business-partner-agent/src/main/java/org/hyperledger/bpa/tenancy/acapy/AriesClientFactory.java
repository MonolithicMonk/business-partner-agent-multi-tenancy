/*
 * Copyright (c) 2020-2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository at
 * https://github.com/hyperledger-labs/business-partner-agent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hyperledger.bpa.tenancy.acapy;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;
import org.hyperledger.aries.AriesClient;
import org.hyperledger.aries.AriesWebSocketClient;
import org.hyperledger.aries.config.UriUtil;
import org.hyperledger.aries.webhook.EventHandler;
import org.hyperledger.bpa.tenancy.acapy.persistence.AcapyTenantConfigRepository;

import org.hyperledger.bpa.tenancy.events.TenantResolved;
import org.hyperledger.bpa.tenancy.resolver.CustomCookieTenantResolver;
import org.hyperledger.bpa.tenancy.events.AcaPyReady;

import java.util.List;

@Factory
@Requires(notEnv = Environment.TEST)
public class AriesClientFactory {

  @Inject
  AcapyTenantConfigRepository acapyTenantConfigRepository;

  @Inject
  ApplicationEventPublisher<AcaPyReady> acaPyReadyEventPublisher;

  @Inject
  ApplicationEventPublisher eventPublisher;

  

  @Value("${bpa.acapy.url}")
  private String url;
  @Value("${bpa.acapy.apiKey}")
  private String apiKey;

  // @Singleton
  // public AriesClient ariesClient() {

  // // Fetch bearerToken for the current tenant
  // String bearerToken = acapyTenantConfigRepository.findByBearerToken()
  // .orElseThrow(() -> new RuntimeException("Bearer token not found for the
  // current tenant"));

  // return AriesClient.builder()
  // .url(url)
  // .apiKey(apiKey)
  // .bearerToken(bearerToken)
  // .build();
  // }

  @Override
  public void onApplicationEvent(TenantResolved event) {
    String tenantId = event.getTenantId();
    String bearerToken = acapyTenantConfigRepository.findByBearerToken(tenantId)
        .orElseThrow(() -> new RuntimeException("Bearer token not found for tenant: " + tenantId));

    AriesClient client = AriesClient.builder()
        .url(url)
        .apiKey(apiKey)
        .bearerToken(bearerToken)
        .build();

    clientMap.put(tenantId, client);
  }

  @Singleton
  public AriesClient ariesClient() {
    String currentTenantId = getCurrentTenantId();
    return clientMap.getOrDefault(currentTenantId, null);
  }

  private String getCurrentTenantId() {
    // Use your tenant resolver to fetch the current tenant ID
    return CustomCookieTenantResolver.resolveTenantIdentifier();
  }
  // @Bean(preDestroy = "shutdown")
  // public AriesWebSocketClient ariesWebSocketClient(List<EventHandler> handlers)
  // {
  // return AriesWebSocketClient.builder()
  // .url(UriUtil.httpToWs(url))
  // .apiKey(apiKey)
  // .handler(handlers)
  // .reactiveBufferSize(20)
  // .build();
  // }

  // @Singleton
  // @Requires(notEnv = Environment.TEST)
  // public record EagerWebsocketClient(@Inject @Getter AriesWebSocketClient ac) {
  // @EventListener
  // public void onServiceStartedEvent(StartupEvent startEvent) {
  // ac.basicMessage(); // only needed to reference the bean so that it is
  // initiated
  // }
  // }
}
