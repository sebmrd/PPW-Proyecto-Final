package ec.ups.edu.proyectofinal.registrations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationStatusRequest(
        @NotBlank
        @Size(max = 20)
        String status
) {
}
