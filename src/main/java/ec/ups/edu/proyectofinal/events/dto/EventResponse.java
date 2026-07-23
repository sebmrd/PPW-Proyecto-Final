package ec.ups.edu.proyectofinal.events.dto;

import ec.ups.edu.proyectofinal.events.entity.Event;

import java.time.Instant;

public record EventResponse(
        Long id,
        String title,
        String description,
        String modality,
        String location,
        String virtualUrl,
        Integer capacity,
        Integer availableCapacity,
        Instant registrationStartAt,
        Instant registrationEndAt,
        Instant startAt,
        Instant endAt,
        String status,
        Boolean deleted,
        Long categoryId,
        String categoryName,
        Long organizerId,
        String organizerEmail,
        Instant createdAt,
        Instant updatedAt
) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getModality(),
                event.getLocation(),
                event.getVirtualUrl(),
                event.getCapacity(),
                event.getAvailableCapacity(),
                event.getRegistrationStartAt(),
                event.getRegistrationEndAt(),
                event.getStartAt(),
                event.getEndAt(),
                event.getStatus(),
                event.getDeleted(),
                event.getCategory().getId(),
                event.getCategory().getName(),
                event.getOrganizer().getId(),
                event.getOrganizer().getEmail(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
