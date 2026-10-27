package com.example.usermanagement.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * API calls count as logged in only when they carry the Bearer token issued to Swagger for this
 * session. Without it (for example after Logout in the Authorize dialog) the request is treated as
 * anonymous and gets 401, even though the browser still holds a session cookie.
 */
public class SwaggerBearerTokenFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(request.getContextPath() + "/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (!hasValidToken(request)) {
            // A fresh context is used so the logged-in context stored in the session is left untouched.
            SecurityContext anonymous = SecurityContextHolder.createEmptyContext();
            anonymous.setAuthentication(new AnonymousAuthenticationToken(
                    "anonymous", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
            SecurityContextHolder.setContext(anonymous);
        }
        chain.doFilter(request, response);
    }

    private boolean hasValidToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String expected = session == null ? null : (String) session.getAttribute(SwaggerSession.API_TOKEN);
        String header = request.getHeader("Authorization");
        if (expected == null || header == null || !header.startsWith(BEARER_PREFIX)) {
            return false;
        }
        String presented = header.substring(BEARER_PREFIX.length()).trim();
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), presented.getBytes(StandardCharsets.UTF_8));
    }
}
