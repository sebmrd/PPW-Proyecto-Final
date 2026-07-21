package ec.ups.edu.proyectofinal.events.repository;

import ec.ups.edu.proyectofinal.events.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    // Para cumplir la regla: "Aplicar paginación, filtros..."
    Page<Event> findByStatusAndIsDeletedFalse(String status, Pageable pageable);
}
