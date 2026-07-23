package ec.ups.edu.proyectofinal.users.service;

import ec.ups.edu.proyectofinal.users.dto.UserSummaryResponse;
import ec.ups.edu.proyectofinal.users.entity.User;
import ec.ups.edu.proyectofinal.users.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;

@Service
public class UserAdminService {

    private static final Set<String> USER_STATUSES = Set.of("ACTIVE", "BLOCKED");

    private final UserRepository userRepository;

    public UserAdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserSummaryResponse> listUsers(String status, String query, Pageable pageable) {
        return userRepository.search(normalizeOptional(status), normalizeSearchQuery(query), pageable)
                .map(UserSummaryResponse::from);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserSummaryResponse updateStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setStatus(normalizeStatus(status));
        user.setUpdatedAt(Instant.now());
        return UserSummaryResponse.from(userRepository.save(user));
    }

    private String normalizeStatus(String status) {
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!USER_STATUSES.contains(normalized)) {
            throw new RuntimeException("Estado de usuario invalido");
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        String normalized = blankToNull(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeSearchQuery(String value) {
        String normalized = blankToNull(value);
        return normalized == null ? "" : normalized;
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
