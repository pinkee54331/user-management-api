package com.example.usermanagement.config;

/** Session attribute names shared by the Swagger Authorize flow. */
public final class SwaggerSession {

    /** App scopes (users:read, ...) ticked in the Authorize dialog for the login in progress. */
    public static final String REQUESTED_SCOPES = "requestedApiScopes";

    /** The "state" Swagger sent when it opened the login popup; echoed back on the redirect. */
    public static final String SWAGGER_STATE = "swaggerOAuthState";

    /** The token handed to Swagger; API calls must present it as a Bearer token. */
    public static final String API_TOKEN = "swaggerApiToken";

    public static final String REDIRECT_PATH = "/swagger-ui/oauth2-redirect.html";
    public static final String TOKEN_PATH = "/swagger-oauth/token";

    private SwaggerSession() {
    }
}
