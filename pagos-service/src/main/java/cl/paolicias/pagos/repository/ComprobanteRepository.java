package cl.paolicias.pagos.repository;

import cl.paolicias.pagos.entities.Comprobante;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {

    Optional<Comprobante> findByReservaId(Long reservaId);
}