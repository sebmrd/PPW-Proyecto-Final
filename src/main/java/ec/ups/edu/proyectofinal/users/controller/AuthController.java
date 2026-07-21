package ec.ups.edu.proyectofinal.users.controller;

import ec.ups.edu.proyectofinal.users.dto.AuthResponse;
import ec.ups.edu.proyectofinal.users.dto.LoginRequest;
import ec.ups.edu.proyectofinal.users.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
