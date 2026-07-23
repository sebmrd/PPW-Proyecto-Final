package ec.ups.edu.proyectofinal.registrations.repository;

import ec.ups.edu.proyectofinal.events.entity.Event;
import ec.ups.edu.proyectofinal.registrations.entity.Registration;
import ec.ups.edu.proyectofinal.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    boolean existsByParticipantAndEvent(User participant, Event event);

    boolean existsByEvent(Event event);

    boolean existsByIdAndParticipant_Email(Long id, String email);

    Optional<Registration> findByIdAndParticipant_Email(Long id, String email);

    Page<Registration> findByParticipant_EmailOrderByRegisteredAtDesc(String email, Pageable pageable);

    Page<Registration> findByEvent_Id(Long eventId, Pageable pageable);
}
