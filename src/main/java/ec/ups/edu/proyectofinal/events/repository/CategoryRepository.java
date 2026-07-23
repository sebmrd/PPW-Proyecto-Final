package ec.ups.edu.proyectofinal.events.repository;

import ec.ups.edu.proyectofinal.events.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Optional<Category> findByIdAndActiveTrue(Long id);

    Page<Category> findByActive(Boolean active, Pageable pageable);
}
