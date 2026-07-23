package ec.ups.edu.proyectofinal.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class SwaggerBasicAuthenticationFilter extends OncePerRequestFilter {

    @Value("${swagger.security.enabled:false}")
    private boolean enabled;

    @Value("${swagger.security.username:evaluator}")
    private String username;

    @Value("${swagger.security.password:evaluator123}")
    private String password;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!isSwaggerRequest(request) || !enabled || hasValidBasicCredentials(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"Swagger\"");
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write("Credenciales de Swagger requeridas");
    }

    private boolean isSwaggerRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/swagger-ui.html")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs");
    }

    private boolean hasValidBasicCredentials(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Basic ")) {
            return false;
        }

        try {
            String credentials = new String(
                    Base64.getDecoder().decode(authorization.substring(6)),
                    StandardCharsets.UTF_8
            );
            int separatorIndex = credentials.indexOf(':');
            if (separatorIndex < 0) {
                return false;
            }

            String providedUsername = credentials.substring(0, separatorIndex);
            String providedPassword = credentials.substring(separatorIndex + 1);
            return constantTimeEquals(providedUsername, username)
                    && constantTimeEquals(providedPassword, password);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean constantTimeEquals(String provided, String expected) {
        return MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8)
        );
    }
}
