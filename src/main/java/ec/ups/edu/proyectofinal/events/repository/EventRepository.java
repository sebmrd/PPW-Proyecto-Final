package ec.ups.edu.proyectofinal.events.repository;

import ec.ups.edu.proyectofinal.events.entity.Event;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByStatusAndDeletedFalse(String status, Pageable pageable);

    long countByDeletedFalseAndCreatedAtBetween(Instant from, Instant to);

    long countByStatusAndDeletedFalseAndCreatedAtBetween(String status, Instant from, Instant to);

    boolean existsByIdAndOrganizer_Email(Long id, String email);

    @Query("""
            select e
            from Event e
            where e.deleted = false
              and (:status is null or e.status = :status)
              and (:categoryId is null or e.category.id = :categoryId)
              and (:query is null or lower(e.title) like lower(concat('%', :query, '%'))
                   or lower(e.description) like lower(concat('%', :query, '%')))
            """)
    Page<Event> search(
            @Param("status") String status,
            @Param("categoryId") Long categoryId,
            @Param("query") String query,
            Pageable pageable
    );

    Optional<Event> findByIdAndDeletedFalse(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.id = :id and e.deleted = false")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);
}
