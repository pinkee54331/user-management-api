package com.example.usermanagement.config;

import com.example.usermanagement.model.Role;
import com.example.usermanagement.model.User;
import com.example.usermanagement.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Accepts a Google login only when the verified email belongs to a stored user
 * whose role is ADMIN or MANAGER, and maps that role to a Spring Security authority.
 */
@Component
public class AdminManagerOidcUserService extends OidcUserService {

    private static final Set<Role> ALLOWED_ROLES = Set.of(Role.ADMIN, Role.MANAGER);

    private final UserRepository repository;

    public AdminManagerOidcUserService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();
        if (email == null || !Boolean.TRUE.equals(oidcUser.getEmailVerified())) {
            throw denied("Google account email is missing or not verified");
        }

        User user = repository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> denied("No user is registered with email " + email));
        if (!ALLOWED_ROLES.contains(user.getRole())) {
            throw denied("User " + email + " has role " + user.getRole() + "; ADMIN or MANAGER required");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        Set<String> requested = requestedScopes();
        ApiScopes.forRole(user.getRole()).stream()
                .filter(scope -> requested == null || requested.contains(scope))
                .forEach(scope -> authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope)));

        return new DefaultOidcUser(
                authorities,
                oidcUser.getIdToken(),
                oidcUser.getUserInfo());
    }

    /** Scopes ticked in the Swagger Authorize dialog for this login, or null when none were requested. */
    @SuppressWarnings("unchecked")
    private static Set<String> requestedScopes() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }
        HttpSession session = attributes.getRequest().getSession(false);
        return session == null ? null
                : (Set<String>) session.getAttribute(SwaggerSession.REQUESTED_SCOPES);
    }

    private static OAuth2AuthenticationException denied(String message) {
        return new OAuth2AuthenticationException(new OAuth2Error("access_denied", message, null), message);
    }
}
