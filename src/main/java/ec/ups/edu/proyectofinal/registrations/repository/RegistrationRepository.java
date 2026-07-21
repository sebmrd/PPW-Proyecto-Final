package ec.ups.edu.proyectofinal.registrations.repository;

import ec.ups.edu.proyectofinal.events.entity.Event;
import ec.ups.edu.proyectofinal.registrations.entity.Registration;
import ec.ups.edu.proyectofinal.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    // Para la regla: "No permitir dos inscripciones del mismo participante"
    boolean existsByParticipantAndEvent(User participant, Event event);
    
    // Para saber si un evento tiene inscripciones antes de eliminarlo
    boolean existsByEvent(Event event);
}
