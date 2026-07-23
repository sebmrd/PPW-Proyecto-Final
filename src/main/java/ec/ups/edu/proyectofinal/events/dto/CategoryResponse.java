package ec.ups.edu.proyectofinal.events.dto;

import ec.ups.edu.proyectofinal.events.entity.Category;

import java.time.Instant;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
