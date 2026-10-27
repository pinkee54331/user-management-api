package com.example.usermanagement.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * After a login started from the Swagger Authorize popup, returns to Swagger's OAuth redirect page so
 * Swagger can finish authorizing (it then asks {@link SwaggerTokenController} for a token). Any other
 * login goes back to the page that was requested, or to Swagger UI.
 */
@Component
public class SwaggerAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public SwaggerAuthenticationSuccessHandler() {
        setDefaultTargetUrl("/swagger-ui.html");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, jakarta.servlet.ServletException {
        HttpSession session = request.getSession(false);
        Object state = session == null ? null : session.getAttribute(SwaggerSession.SWAGGER_STATE);
        if (state == null) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }
        session.removeAttribute(SwaggerSession.SWAGGER_STATE);
        clearAuthenticationAttributes(request);
        // The code is not checked: the token endpoint relies on the session cookie, not on the code.
        String target = request.getContextPath() + SwaggerSession.REDIRECT_PATH
                + "?code=session&state=" + URLEncoder.encode(state.toString(), StandardCharsets.UTF_8);
        getRedirectStrategy().sendRedirect(request, response, target);
    }
}
