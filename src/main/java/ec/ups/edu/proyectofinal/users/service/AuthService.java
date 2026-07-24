package ec.ups.edu.proyectofinal.users.service;

import ec.ups.edu.proyectofinal.exception.RateLimitExceededException;
import ec.ups.edu.proyectofinal.security.JwtService;
import ec.ups.edu.proyectofinal.users.dto.AuthResponse;
import ec.ups.edu.proyectofinal.users.dto.LoginRequest;
import ec.ups.edu.proyectofinal.users.dto.RefreshTokenRequest;
import ec.ups.edu.proyectofinal.users.dto.RegisterRequest;
import ec.ups.edu.proyectofinal.users.dto.UserProfileResponse;
import ec.ups.edu.proyectofinal.users.entity.RefreshToken;
import ec.ups.edu.proyectofinal.users.entity.Role;
import ec.ups.edu.proyectofinal.users.entity.RoleEnum;
import ec.ups.edu.proyectofinal.users.entity.User;
import ec.ups.edu.proyectofinal.users.repository.RefreshTokenRepository;
import ec.ups.edu.proyectofinal.users.repository.RoleRepository;
import ec.ups.edu.proyectofinal.users.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 3;
    private static final Duration FAILED_LOGIN_WINDOW = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            StringRedisTemplate redisTemplate
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("El correo ya se encuentra registrado");
        }

        Role participantRole = roleRepository.findByName(RoleEnum.PARTICIPANT)
                .orElseThrow(() -> new RuntimeException("Rol PARTICIPANT no configurado"));

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus("ACTIVE");
        user.getRoles().add(participantRole);

        return issueTokens(userRepository.save(user), null, ipAddress);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        String email = normalizeEmail(request.getEmail());
        String blockKey = "blocked-user:" + email;
        String attemptKey = "login-attempts:" + email;

        if (isTemporarilyBlocked(blockKey)) {
            throw temporaryLoginBlockException();
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (AuthenticationException e) {
            registerFailedLoginAttempt(attemptKey, blockKey);
            throw new BadCredentialsException("Credenciales invalidas");
        }

        clearFailedLoginAttempts(attemptKey);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        ensureActive(user);

        return issueTokens(user, null, ipAddress);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request, String ipAddress) {
        RefreshToken storedToken = findValidStoredRefreshToken(request.getRefreshToken());
        return issueTokens(storedToken.getUser(), storedToken, ipAddress);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.getRefreshToken());
        refreshTokenRepository.findByTokenHash(tokenHash)
                .filter(token -> token.getRevokedAt() == null)
                .ifPresent(token -> {
                    token.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional(readOnly = true)
    public UserProfileResponse me(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        ensureActive(user);

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .sorted()
                .toList();

        return new UserProfileResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getStatus(),
                roles
        );
    }

    private AuthResponse issueTokens(User user, RefreshToken tokenToRevoke, String ipAddress) {
        UUID refreshTokenId = UUID.randomUUID();
        Instant now = Instant.now();

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, refreshTokenId);

        RefreshToken storedToken = new RefreshToken();
        storedToken.setTokenId(refreshTokenId);
        storedToken.setUser(user);
        storedToken.setTokenHash(hashToken(refreshToken));
        storedToken.setExpiresAt(now.plusMillis(jwtService.getRefreshExpirationMillis()));
        storedToken.setCreatedAt(now);
        storedToken.setCreatedByIp(ipAddress);

        if (tokenToRevoke != null) {
            tokenToRevoke.setRevokedAt(now);
            tokenToRevoke.setReplacedByTokenId(refreshTokenId);
            refreshTokenRepository.save(tokenToRevoke);
        }

        refreshTokenRepository.save(storedToken);

        return new AuthResponse(accessToken, refreshToken);
    }

    private RefreshToken findValidStoredRefreshToken(String rawToken) {
        try {
            String email = jwtService.extractUsername(rawToken);
            UUID tokenId = jwtService.extractTokenId(rawToken);
            String tokenHash = hashToken(rawToken);

            User user = userRepository.findByEmail(normalizeEmail(email))
                    .orElseThrow(() -> new BadCredentialsException("Refresh token invalido"));

            ensureActive(user);

            RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                    .orElseThrow(() -> new BadCredentialsException("Refresh token invalido"));

            boolean invalidStoredToken = !storedToken.getTokenId().equals(tokenId)
                    || !storedToken.getUser().getId().equals(user.getId())
                    || storedToken.getRevokedAt() != null
                    || storedToken.getExpiresAt().isBefore(Instant.now())
                    || !jwtService.isTokenValid(rawToken, user);

            if (invalidStoredToken) {
                throw new BadCredentialsException("Refresh token invalido");
            }

            return storedToken;
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("Refresh token invalido");
        }
    }

    private void ensureActive(User user) {
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new RuntimeException("La cuenta esta deshabilitada");
        }
    }

    private boolean isTemporarilyBlocked(String blockKey) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(blockKey));
        } catch (DataAccessException e) {
            return false;
        }
    }

    private void registerFailedLoginAttempt(String attemptKey, String blockKey) {
        try {
            Long attempts = redisTemplate.opsForValue().increment(attemptKey);

            if (attempts != null && attempts == 1) {
                redisTemplate.expire(attemptKey, FAILED_LOGIN_WINDOW);
            }

            if (attempts != null && attempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
                redisTemplate.opsForValue().set(blockKey, "BLOCKED", FAILED_LOGIN_WINDOW);
                redisTemplate.delete(attemptKey);
                throw temporaryLoginBlockException();
            }
        } catch (DataAccessException e) {
            // Keep login usable if Redis is unavailable in local development.
        }
    }

    private void clearFailedLoginAttempts(String attemptKey) {
        try {
            redisTemplate.delete(attemptKey);
        } catch (DataAccessException e) {
            // Keep login usable if Redis is unavailable in local development.
        }
    }

    private RateLimitExceededException temporaryLoginBlockException() {
        return new RateLimitExceededException(
                "Cuenta bloqueada temporalmente por multiples intentos fallidos. Intente mas tarde.",
                FAILED_LOGIN_WINDOW.toSeconds()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo calcular el hash del refresh token", e);
        }
    }
}
