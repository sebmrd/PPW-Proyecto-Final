package ec.ups.edu.proyectofinal.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre no puede superar 80 caracteres")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 80, message = "El apellido no puede superar 80 caracteres")
    private String lastName;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo invalido")
    @Size(max = 160, message = "El correo no puede superar 160 caracteres")
    private String email;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres")
    private String password;
}
