package com.example.usermanagement.config;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The "token URL" of the Swagger Authorize dialog. Swagger posts here after the login popup returns;
 * the logged-in session gets a random token that Swagger then sends as a Bearer token on API calls.
 * Logging out in the dialog makes Swagger drop the token, which cuts off API access.
 */
@Hidden
@RestController
public class SwaggerTokenController {

    private static final String SCOPE_PREFIX = "SCOPE_";

    private final SecureRandom random = new SecureRandom();

    @PostMapping(SwaggerSession.TOKEN_PATH)
    public ResponseEntity<Map<String, String>> token(Authentication authentication, HttpSession session) {
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(SwaggerSession.API_TOKEN, token);

        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith(SCOPE_PREFIX))
                .map(a -> a.substring(SCOPE_PREFIX.length()))
                .sorted()
                .collect(Collectors.joining(" "));
        return ResponseEntity.ok(Map.of("access_token", token, "token_type", "Bearer", "scope", scope));
    }
}
