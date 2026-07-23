package ec.ups.edu.proyectofinal.registrations.dto;

import ec.ups.edu.proyectofinal.registrations.entity.Registration;

import java.time.Instant;
import java.util.UUID;

public record RegistrationResponse(
        Long id,
        UUID registrationCode,
        Long eventId,
        String eventTitle,
        Long participantId,
        String participantEmail,
        String status,
        Instant registeredAt,
        Instant statusUpdatedAt,
        Instant confirmedAt,
        Instant cancelledAt
) {
    public static RegistrationResponse from(Registration registration) {
        return new RegistrationResponse(
                registration.getId(),
                registration.getRegistrationCode(),
                registration.getEvent().getId(),
                registration.getEvent().getTitle(),
                registration.getParticipant().getId(),
                registration.getParticipant().getEmail(),
                registration.getStatus(),
                registration.getRegisteredAt(),
                registration.getStatusUpdatedAt(),
                registration.getConfirmedAt(),
                registration.getCancelledAt()
        );
    }
}
