package ec.ups.edu.proyectofinal.users.repository;

import ec.ups.edu.proyectofinal.users.entity.Role;
import ec.ups.edu.proyectofinal.users.entity.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleEnum name);
}
