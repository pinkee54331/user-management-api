package com.example.usermanagement.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles a login started from the Swagger "Authorize" dialog. Swagger opens the login URL with the
 * ticked app scopes (users:read, ...) in "scope" and its own "state". Both are kept in the session
 * until the Google login completes: {@link AdminManagerOidcUserService} then grants only the
 * requested scopes the user's role allows, and {@link SwaggerAuthenticationSuccessHandler} sends the
 * popup back to Swagger with that state. A login without these parameters gets the full set of scopes.
 */
@Component
public class ScopeAwareAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private static final String AUTHORIZATION_PATH = "/oauth2/authorization";
    private static final Set<String> KNOWN_SCOPES =
            Set.of(ApiScopes.USERS_READ, ApiScopes.USERS_WRITE, ApiScopes.USERS_DELETE);

    private final DefaultOAuth2AuthorizationRequestResolver delegate;

    public ScopeAwareAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrations) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrations, AUTHORIZATION_PATH);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        rememberSwaggerRequest(request);
        return delegate.resolve(request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        rememberSwaggerRequest(request);
        return delegate.resolve(request, clientRegistrationId);
    }

    private void rememberSwaggerRequest(HttpServletRequest request) {
        // The resolver is asked about every request; only the login start URL matters here.
        if (!request.getRequestURI().startsWith(request.getContextPath() + AUTHORIZATION_PATH + "/")) {
            return;
        }
        HttpSession session = request.getSession();

        String scope = request.getParameter("scope");
        if (scope == null || scope.isBlank()) {
            session.removeAttribute(SwaggerSession.REQUESTED_SCOPES);
        } else {
            Set<String> requested = Arrays.stream(scope.split("[\\s,]+"))
                    .filter(KNOWN_SCOPES::contains)
                    .collect(Collectors.toSet());
            session.setAttribute(SwaggerSession.REQUESTED_SCOPES, requested);
        }

        // Swagger sends redirect_uri and state; a plain browser login does not.
        String state = request.getParameter("state");
        if (state != null && request.getParameter("redirect_uri") != null) {
            session.setAttribute(SwaggerSession.SWAGGER_STATE, state);
        } else {
            session.removeAttribute(SwaggerSession.SWAGGER_STATE);
        }
    }
}
