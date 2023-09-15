package org.hyperledger.bpa.tenancy.security;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.config.RedirectService;
import io.micronaut.security.config.RedirectConfiguration;
import io.micronaut.security.token.jwt.generator.AccessRefreshTokenGenerator;
import io.micronaut.security.token.jwt.generator.AccessTokenConfiguration;
import io.micronaut.security.token.jwt.render.AccessRefreshToken;
import io.micronaut.security.errors.PriorToLoginPersistence;
import io.micronaut.security.token.jwt.cookie.RefreshTokenCookieConfiguration;
import io.micronaut.security.token.jwt.cookie.AccessTokenCookieConfiguration;
import io.micronaut.security.token.jwt.cookie.JwtCookieLoginHandler;
import io.micronaut.security.config.SecurityConfigurationProperties;
import io.micronaut.multitenancy.tenantresolver.CookieTenantResolverConfiguration;
import io.micronaut.security.errors.OauthErrorResponseException;
import io.micronaut.security.errors.ObtainingAuthorizationErrorCode;

import io.micronaut.context.annotation.Value;

import jakarta.inject.Inject;

import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Singleton;
import io.micronaut.context.annotation.Requires;

/**
 * Custom JWT Cookie Login Handler to extend the functionalities. It overrides
 * the default JwtCookieLoginHandler.
 *
 * @author Yuki I
 * @since 1.0
 */
@Replaces(JwtCookieLoginHandler.class)
@Requires(property = SecurityConfigurationProperties.PREFIX + ".authentication", value = "cookie")
@Singleton
public class TenantandJwtCookieLoginHandler extends JwtCookieLoginHandler {

    @Value("${tenancy.jwtTenantAttribute}")
    String jwtTenantAttribute;

    @Inject
    CookieTenantResolverConfiguration cookieTenantResolverConfiguration;

    @Inject
    public TenantandJwtCookieLoginHandler(RedirectService redirectService,
            RedirectConfiguration redirectConfiguration,
            AccessTokenCookieConfiguration accessTokenCookieConfiguration,
            RefreshTokenCookieConfiguration refreshTokenCookieConfiguration,
            AccessTokenConfiguration accessTokenConfiguration,
            AccessRefreshTokenGenerator accessRefreshTokenGenerator,
            @Nullable PriorToLoginPersistence priorToLoginPersistence) {
        super(redirectService, redirectConfiguration, accessTokenCookieConfiguration,
                refreshTokenCookieConfiguration, accessTokenConfiguration, accessRefreshTokenGenerator,
                priorToLoginPersistence);
    }

    @Override
    public List<Cookie> getCookies(Authentication authentication, HttpRequest<?> request) {
        AccessRefreshToken accessRefreshToken = accessRefreshTokenGenerator.generate(authentication)
                .orElseThrow(() -> new OauthErrorResponseException(ObtainingAuthorizationErrorCode.SERVER_ERROR,
                        "Cannot obtain an access token", null));

        return customGetCookies(accessRefreshToken, request, authentication);
    }

    @Override
    public List<Cookie> getCookies(Authentication authentication, String refreshToken, HttpRequest<?> request) {
        AccessRefreshToken accessRefreshToken = accessRefreshTokenGenerator.generate(refreshToken, authentication)
                .orElseThrow(() -> new OauthErrorResponseException(ObtainingAuthorizationErrorCode.SERVER_ERROR,
                        "Cannot obtain an access token", null));

        return customGetCookies(accessRefreshToken, request, authentication);
    }

    /**
     * Return the cookies for the given parameters. This method enhances the base
     * implementation by also adding a cookie named 'tenantid' with a value of
     * 'sampletenant'.
     *
     * @param accessRefreshToken The access refresh token.
     * @param request            The current request.
     * @param authentication     The authentication object
     * @return A list of cookies.
     */
    protected List<Cookie> customGetCookies(AccessRefreshToken accessRefreshToken,
            HttpRequest<?> request,
            Authentication authentication) {

        List<Cookie> cookies = new ArrayList<>(3);
        Cookie jwtCookie = Cookie.of(accessTokenCookieConfiguration.getCookieName(),
                accessRefreshToken.getAccessToken());
        jwtCookie.configure(accessTokenCookieConfiguration, request.isSecure());
        TemporalAmount maxAge = accessTokenCookieConfiguration.getCookieMaxAge()
                .orElseGet(() -> Duration.ofSeconds(accessTokenConfiguration.getExpiration()));
        jwtCookie.maxAge(maxAge);

        cookies.add(jwtCookie);

        String refreshToken = accessRefreshToken.getRefreshToken();
        if (StringUtils.isNotEmpty(refreshToken)) {
            Cookie refreshCookie = Cookie.of(refreshTokenCookieConfiguration.getCookieName(), refreshToken);
            refreshCookie.configure(refreshTokenCookieConfiguration, request.isSecure());
            refreshCookie
                    .maxAge(refreshTokenCookieConfiguration.getCookieMaxAge().orElseGet(() -> Duration.ofDays(30)));
            cookies.add(refreshCookie);
        }

        String tenantString = authentication.getAttributes().get(jwtTenantAttribute).toString();
        // Add the custom tenant cookie
        Cookie tenantCookie = Cookie.of(cookieTenantResolverConfiguration.getCookiename(), tenantString);
        tenantCookie.configure(accessTokenCookieConfiguration, request.isSecure());
        tenantCookie.maxAge(accessTokenCookieConfiguration.getCookieMaxAge().orElseGet(() -> Duration.ofDays(30)));

        cookies.add(tenantCookie);

        return cookies;
    }
}
