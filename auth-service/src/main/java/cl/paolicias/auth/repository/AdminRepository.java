package cl.paolicias.auth.repository;

import cl.paolicias.auth.entities.Administrador;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Administrador, Long> {

    Optional<Administrador> findByEmail(String email);
}