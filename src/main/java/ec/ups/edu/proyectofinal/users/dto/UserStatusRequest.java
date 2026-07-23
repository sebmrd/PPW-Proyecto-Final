package ec.ups.edu.proyectofinal.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserStatusRequest(
        @NotBlank
        @Size(max = 20)
        String status
) {
}
