package ec.ups.edu.proyectofinal.users.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String status;
    private List<String> roles;
}
