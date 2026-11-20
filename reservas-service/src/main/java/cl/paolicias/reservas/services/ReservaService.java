package cl.paolicias.reservas.services;

import cl.paolicias.reservas.dto.ReservaRequest;
import cl.paolicias.reservas.entities.EstadoReserva;
import cl.paolicias.reservas.entities.Reserva;
import java.util.List;

public interface ReservaService {

    Reserva crear(ReservaRequest request);

    List<Reserva> listarPorEmail(String email);

    List<Reserva> listarTodas();

    Reserva obtener(Long id);

    Reserva cambiarEstado(Long id, String claveInterna, EstadoReserva estado);
}