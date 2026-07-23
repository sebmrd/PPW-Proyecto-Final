package ec.ups.edu.proyectofinal.registrations.repository;

import ec.ups.edu.proyectofinal.registrations.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByEvent_IdOrderByStartAtAsc(Long eventId);

    Optional<Session> findByIdAndEvent_Id(Long id, Long eventId);
}
