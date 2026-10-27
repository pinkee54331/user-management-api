package com.example.usermanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String GOOGLE_LOGIN = "googleLogin";

    @Bean
    public OpenAPI userManagementOpenAPI() {
        // The authorization URL starts the app's own Google login. Swagger passes the ticked scopes in
        // the "scope" parameter; ScopeAwareAuthorizationRequestResolver limits the session to them.
        OAuthFlow flow = new OAuthFlow()
                .authorizationUrl("/oauth2/authorization/google")
                .tokenUrl(SwaggerSession.TOKEN_PATH)
                .scopes(new Scopes()
                        .addString(ApiScopes.USERS_READ, "Read users (ADMIN, MANAGER)")
                        .addString(ApiScopes.USERS_WRITE, "Create and update users (ADMIN, MANAGER)")
                        .addString(ApiScopes.USERS_DELETE, "Delete users (ADMIN)"));

        return new OpenAPI()
                .info(new Info()
                        .title("User Management API")
                        .description("REST API for managing user data.")
                        .version("v1"))
                .components(new Components().addSecuritySchemes(GOOGLE_LOGIN, new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2)
                        .description("Tick the scopes you want and click Authorize to sign in with Google. "
                                + "You only receive the ticked scopes that your role allows. "
                                + "Leave client_id and client_secret as they are; they are not used. "
                                + "After authorizing, this dialog shows Logout, which stops Swagger from "
                                + "sending the token so API calls return 401. "
                                + "Without a token API calls return 401; a missing scope returns 403.")
                        .flows(new OAuthFlows().authorizationCode(flow))));
    }
}
