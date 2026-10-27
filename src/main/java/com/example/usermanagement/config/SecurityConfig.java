package com.example.usermanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.nio.charset.StandardCharsets;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            AdminManagerOidcUserService oidcUserService,
                                            ScopeAwareAuthorizationRequestResolver authorizationRequestResolver,
                                            SwaggerAuthenticationSuccessHandler successHandler) throws Exception {
        // Ant matchers are used explicitly because the H2 console servlet makes
        // string patterns ambiguous between Spring MVC and servlet mappings.
        RequestMatcher swagger = new OrRequestMatcher(
                antMatcher("/swagger-ui.html"),
                antMatcher("/swagger-ui/**"),
                antMatcher("/v3/api-docs/**"));

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(swagger).hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(antMatcher(HttpMethod.GET, "/api/users/**"))
                                .hasAuthority("SCOPE_" + ApiScopes.USERS_READ)
                        .requestMatchers(antMatcher(HttpMethod.POST, "/api/users/**"),
                                antMatcher(HttpMethod.PUT, "/api/users/**"))
                                .hasAuthority("SCOPE_" + ApiScopes.USERS_WRITE)
                        .requestMatchers(antMatcher(HttpMethod.DELETE, "/api/users/**"))
                                .hasAuthority("SCOPE_" + ApiScopes.USERS_DELETE)
                        // Any other /api call (new controllers, other methods) is denied until given a scope above.
                        .requestMatchers(antMatcher("/api/**")).denyAll()
                        .anyRequest().permitAll())
                // API clients get 401; everything else (Swagger UI) is sent to the Google login.
                // Order matters: the first matching entry point wins.
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), antMatcher("/api/**"))
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/oauth2/authorization/google"),
                                AnyRequestMatcher.INSTANCE))
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(endpoint -> endpoint.authorizationRequestResolver(authorizationRequestResolver))
                        .successHandler(successHandler)
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService))
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(403);
                            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.getWriter().write("Access denied: " + exception.getMessage());
                        }))
                // Rejects /api calls that lack the Bearer token Swagger got from the Authorize dialog.
                .addFilterBefore(new SwaggerBearerTokenFilter(), AuthorizationFilter.class)
                // Ends the whole server session. Logout in the Authorize dialog only drops Swagger's token.
                .logout(logout -> logout
                        .logoutRequestMatcher(antMatcher(HttpMethod.GET, "/logout"))
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setContentType(MediaType.TEXT_HTML_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.getWriter().write(
                                    "You have been logged out. <a href=\"/swagger-ui.html\">Log in again</a>");
                        }))
                // Swagger "Try it out" calls carry no CSRF token. The session cookie is SameSite=Lax
                // (see application.properties), so browsers do not send it on cross-site writes.
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        new AntPathRequestMatcher("/api/**"),
                        new AntPathRequestMatcher(SwaggerSession.TOKEN_PATH),
                        new AntPathRequestMatcher("/h2-console/**")))
                // The H2 console renders in frames.
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
