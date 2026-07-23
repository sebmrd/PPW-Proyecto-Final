package ec.ups.edu.proyectofinal.registrations.dto;

import ec.ups.edu.proyectofinal.registrations.entity.Session;

import java.time.Instant;

public record SessionResponse(
        Long id,
        Long eventId,
        String title,
        String description,
        Instant startAt,
        Instant endAt,
        String location,
        String virtualUrl,
        Instant createdAt,
        Instant updatedAt
) {
    public static SessionResponse from(Session session) {
        return new SessionResponse(
                session.getId(),
                session.getEvent().getId(),
                session.getTitle(),
                session.getDescription(),
                session.getStartAt(),
                session.getEndAt(),
                session.getLocation(),
                session.getVirtualUrl(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }
}
