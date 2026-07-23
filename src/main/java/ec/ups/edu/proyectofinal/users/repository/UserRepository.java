package ec.ups.edu.proyectofinal.users.repository;

import ec.ups.edu.proyectofinal.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
            select u
            from User u
            where (:status is null or u.status = :status)
              and (:query is null or lower(u.email) like lower(concat('%', :query, '%'))
                   or lower(u.firstName) like lower(concat('%', :query, '%'))
                   or lower(u.lastName) like lower(concat('%', :query, '%')))
            """)
    Page<User> search(@Param("status") String status, @Param("query") String query, Pageable pageable);
}
