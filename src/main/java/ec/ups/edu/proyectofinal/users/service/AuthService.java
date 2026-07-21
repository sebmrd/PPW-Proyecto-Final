package ec.ups.edu.proyectofinal.users.service;

import ec.ups.edu.proyectofinal.security.JwtService;
import ec.ups.edu.proyectofinal.users.dto.AuthResponse;
import ec.ups.edu.proyectofinal.users.dto.LoginRequest;
import ec.ups.edu.proyectofinal.users.entity.User;
import ec.ups.edu.proyectofinal.users.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            // Cumpliendo rúbrica: Mensaje genérico para no revelar existencia de correo
            throw new RuntimeException("Credenciales inválidas"); 
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!user.getStatus()) {
            throw new RuntimeException("La cuenta está deshabilitada");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }
}
