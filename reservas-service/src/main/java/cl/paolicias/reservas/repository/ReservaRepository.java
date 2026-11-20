package cl.paolicias.reservas.repository;

import cl.paolicias.reservas.entities.Reserva;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByClienteEmailIgnoreCaseOrderByIdDesc(String clienteEmail);

    List<Reserva> findAllByOrderByIdDesc();
}