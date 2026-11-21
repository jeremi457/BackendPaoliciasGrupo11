package cl.paolicias.pagos.repository;

import cl.paolicias.pagos.entities.Pago;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByReservaId(Long reservaId);
}