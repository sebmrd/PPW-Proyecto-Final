package ec.ups.edu.proyectofinal.users.dto;

import ec.ups.edu.proyectofinal.users.entity.User;

import java.time.Instant;
import java.util.List;

public record UserSummaryResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String status,
        List<String> roles,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getStatus(),
                user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .sorted()
                        .toList(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
