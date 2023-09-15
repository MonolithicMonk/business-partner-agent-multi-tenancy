package org.hyperledger.bpa.tenancy.controller;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpMethod;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.filters.AuthenticationFetcher;
import io.reactivex.rxjava3.core.Maybe;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.hyperledger.bpa.tenancy.security.roles.RolesandPermissions;
import org.hyperledger.bpa.constants.TenancyConstants;
import org.hyperledger.bpa.tenancy.controller.AriesWebhookController;

import org.apache.commons.lang3.StringUtils;
import jakarta.inject.Inject;
import java.util.List;

@Slf4j
@Singleton
@Requires(property = "tenancy.apiKey")
public class AriesWebhookAuthFetcher implements AuthenticationFetcher {

  
  @Value("${tenancy.apiKey}")
  String apiKey;
  
  @Inject
  TenancyConstants tenancyConstants;
  
  @Override
  public Publisher<Authentication> fetchAuthentication(io.micronaut.http.HttpRequest<?> request) {
    
        String X_API_KEY = tenancyConstants.ACAPY_ADMIN_API_KEY_HEADER_NAME;

        return Maybe.<Authentication>create(emitter -> {
            if (HttpMethod.POST.equals(request.getMethod())
                    && request.getPath().startsWith(AriesWebhookController.WEBHOOK_CONTROLLER_PATH)) {
                String apiKeyHeader = request.getHeaders().get(X_API_KEY);
                log.debug("Handling acapy events webhook authentication");

                if (StringUtils.isNotBlank(apiKey) && apiKeyHeader.equals(apiKey)) {
                    emitter.onSuccess(tenancyAuthentication());
                    log.debug("acapy events webhook authentication success");
                    return;
                }

                log.error("acapy events webhook authentication failed. " +
                        "Configured tenancy.apiKey: {}, received x-api-key header: {}", apiKey, apiKeyHeader);
                emitter.onError(new IllegalArgumentException("Invalid API key"));
            } else {
                emitter.onComplete();
            }
        }).toFlowable();
    }

    static Authentication tenancyAuthentication() {
        return Authentication.build("acapy-webhooks", List.of(RolesandPermissions.ROLE_TENANT_ADMIN));
    }
}
