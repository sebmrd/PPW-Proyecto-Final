package ec.ups.edu.proyectofinal.users.controller;

import ec.ups.edu.proyectofinal.users.dto.AuthResponse;
import ec.ups.edu.proyectofinal.users.dto.LoginRequest;
import ec.ups.edu.proyectofinal.users.dto.RefreshTokenRequest;
import ec.ups.edu.proyectofinal.users.dto.RegisterRequest;
import ec.ups.edu.proyectofinal.users.dto.UserProfileResponse;
import ec.ups.edu.proyectofinal.users.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request, clientIp(servletRequest)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authService.login(request, clientIp(servletRequest)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authService.refresh(request, clientIp(servletRequest)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(Authentication authentication) {
        return ResponseEntity.ok(authService.me(authentication.getName()));
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
