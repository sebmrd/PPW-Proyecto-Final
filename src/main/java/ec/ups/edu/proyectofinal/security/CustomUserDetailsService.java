package ec.ups.edu.proyectofinal.security;

import ec.ups.edu.proyectofinal.users.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // La rúbrica dice: "Mensajes de autenticación genéricos para evitar revelar si un correo existe"
        // Por ende, lanzamos un mensaje genérico incluso si no se encuentra.
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas")); 
    }
}
