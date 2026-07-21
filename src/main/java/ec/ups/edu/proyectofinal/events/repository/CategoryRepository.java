package ec.ups.edu.proyectofinal.events.repository;

import ec.ups.edu.proyectofinal.events.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Para cumplir la regla: "No permitir categorías duplicadas"
    boolean existsByName(String name);
}
