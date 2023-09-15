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
import org.hyperledger.bpa.tenancy.controller.TenancyController;

import org.apache.commons.lang3.StringUtils;
import java.util.List;

@Slf4j
@Singleton
@Requires(property = "tenancy.apiKey")
public class TenancyAuthFetcher implements AuthenticationFetcher {

    private static final String X_API_KEY = "x-api-key";

    @Value("${tenancy.apiKey}")
    String apiKey;

    String tenantPath = TenancyController.TENANCY_CONTROLLER_PATH;

    @Override
    public Publisher<Authentication> fetchAuthentication(io.micronaut.http.HttpRequest<?> request) {
        return Maybe.<Authentication>create(emitter -> {
            if (HttpMethod.POST.equals(request.getMethod())
                    && request.getPath().startsWith(tenantPath)) {
                String apiKeyHeader = request.getHeaders().get(X_API_KEY);
                log.debug("Handling tenancy-admin tenancy authentication");

                if (StringUtils.isNotBlank(apiKey) && apiKeyHeader.equals(apiKey)) {
                    emitter.onSuccess(tenancyAuthentication());
                    log.debug("tenancy-admin tenancy authentication success");
                    return;
                }

                log.error("tenancy-admin tenancy authentication failed. " +
                        "Configured tenancy.apiKey: {}, received x-api-key header: {}", apiKey, apiKeyHeader);
                emitter.onError(new IllegalArgumentException("Invalid API key"));
            } else {
                emitter.onComplete();
            }
        }).toFlowable();
    }

    static Authentication tenancyAuthentication() {
        return Authentication.build("tenancy-admin", List.of(RolesandPermissions.ROLE_TENANT_ADMIN));
    }
}
