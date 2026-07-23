package ec.ups.edu.proyectofinal.registrations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record SessionRequest(
        @NotBlank
        @Size(max = 160)
        String title,

        String description,

        @NotNull
        Instant startAt,

        @NotNull
        Instant endAt,

        @Size(max = 200)
        String location,

        @Size(max = 500)
        String virtualUrl
) {
}
