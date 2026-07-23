package ec.ups.edu.proyectofinal.events.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record EventRequest(
        @NotBlank
        @Size(max = 160)
        String title,

        @NotBlank
        String description,

        @NotBlank
        @Size(max = 20)
        String modality,

        @Size(max = 200)
        String location,

        @Size(max = 500)
        String virtualUrl,

        @NotNull
        @Min(1)
        Integer capacity,

        @NotNull
        Instant registrationStartAt,

        @NotNull
        Instant registrationEndAt,

        @NotNull
        Instant startAt,

        @NotNull
        Instant endAt,

        @NotNull
        Long categoryId
) {
}
