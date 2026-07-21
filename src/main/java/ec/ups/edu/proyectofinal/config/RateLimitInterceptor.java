package ec.ups.edu.proyectofinal.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.lang.NonNull;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitingService rateLimitingService;

    public RateLimitInterceptor(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        String user = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null;

        // 1. Inicio de Sesión (5 por minuto por IP y correo)
        if (uri.equals("/api/auth/login") && request.getMethod().equals("POST")) {
            // Idealmente el correo se extraería del body, pero para simplicidad interceptamos por IP
            rateLimitingService.checkRateLimit("rate:login:" + ip, 5, 60, "Inicio de Sesión");
            return true;
        }

        // 2. Registro (3 por hora por IP)
        if (uri.equals("/api/auth/register") && request.getMethod().equals("POST")) {
            rateLimitingService.checkRateLimit("rate:register:" + ip, 3, 3600, "Registro");
            return true;
        }

        // 3. Generación de reportes (5 por minuto por Usuario autenticado)
        if (uri.startsWith("/api/reports/")) {
            if (user != null) {
                rateLimitingService.checkRateLimit("rate:reports:" + user, 5, 60, "Reportes");
            }
            return true;
        }

        // 4. Endpoints Autenticados (120 por minuto)
        if (user != null) {
            rateLimitingService.checkRateLimit("rate:auth:" + user, 120, 60, "Endpoint Autenticado");
        } 
        // 5. Endpoints Públicos (60 por minuto)
        else {
            rateLimitingService.checkRateLimit("rate:public:" + ip, 60, 60, "Endpoint Público");
        }

        return true;
    }
}
