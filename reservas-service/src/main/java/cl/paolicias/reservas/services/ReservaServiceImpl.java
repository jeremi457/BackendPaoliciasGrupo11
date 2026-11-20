package cl.paolicias.reservas.services;

import cl.paolicias.reservas.client.CatalogoClient;
import cl.paolicias.reservas.client.ProductoInfo;
import cl.paolicias.reservas.dto.ReservaRequest;
import cl.paolicias.reservas.entities.EstadoReserva;
import cl.paolicias.reservas.entities.Reserva;
import cl.paolicias.reservas.repository.ReservaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService {

    private static final Set<String> HORARIOS = Set.of("Mañana", "Tarde", "Noche");
    private static final ZoneId ZONA = ZoneId.of("America/Santiago");

    private final ReservaRepository repository;
    private final CatalogoClient catalogoClient;

    @Value("${app.internal-key}")
    private String claveInterna;

    @Override
    public Reserva crear(ReservaRequest r) {
        if (r.clienteEmail() == null || !r.clienteEmail().contains("@")) {
            throw error("Falta el correo del cliente.");
        }
        if (r.productoId() == null) {
            throw error("Selecciona un producto.");
        }

        LocalDate fecha = parsearFecha(r.fecha());
        if (fecha.isBefore(LocalDate.now(ZONA))) {
            throw error("La fecha no puede ser anterior a hoy.");
        }
        if (r.horario() == null || !HORARIOS.contains(r.horario())) {
            throw error("Selecciona un horario válido.");
        }
        if (r.asistentes() == null || r.asistentes() < 10 || r.asistentes() > 100
                || r.asistentes() % 10 != 0) {
            throw error("Los asistentes deben ser de 10 en 10, entre 10 y 100.");
        }
        if (r.telefono() == null || !r.telefono().matches("\\d{9}")) {
            throw error("Ingresa un teléfono válido de 9 dígitos.");
        }
        String observaciones = r.observaciones() == null ? "" : r.observaciones().trim();
        if (observaciones.length() > 200) {
            throw error("Las observaciones admiten máximo 200 caracteres.");
        }

        // El precio lo decide el catálogo, no la app
        ProductoInfo producto = catalogoClient.obtenerProducto(r.productoId());
        if (!Boolean.TRUE.equals(producto.activo())) {
            throw error("El producto no está disponible.");
        }

        int total = producto.precioBase() * (r.asistentes() / 10);
        int abono = total / 2;

        Reserva reserva = new Reserva();
        reserva.setClienteEmail(r.clienteEmail().trim().toLowerCase());
        reserva.setProductoId(producto.id());
        reserva.setProductoNombre(producto.nombre());
        reserva.setFecha(fecha);
        reserva.setHorario(r.horario());
        reserva.setAsistentes(r.asistentes());
        reserva.setTelefono(r.telefono());
        reserva.setObservaciones(observaciones);
        reserva.setTotal(total);
        reserva.setAbono(abono);
        reserva.setEstado(EstadoReserva.PENDIENTE_PAGO);
        reserva.setCreadaEn(LocalDateTime.now(ZONA));
        return repository.save(reserva);
    }

    @Override
    public List<Reserva> listarPorEmail(String email) {
        if (email == null || email.isBlank()) {
            throw error("Falta el correo del cliente.");
        }
        return repository.findByClienteEmailIgnoreCaseOrderByIdDesc(email.trim());
    }

    @Override
    public List<Reserva> listarTodas() {
        return repository.findAllByOrderByIdDesc();
    }

    @Override
    public Reserva obtener(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada."));
    }

    @Override
    public Reserva cambiarEstado(Long id, String clave, EstadoReserva estado) {
        if (clave == null || !clave.equals(claveInterna)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Operación permitida solo entre servicios.");
        }
        if (estado == null) {
            throw error("Indica el nuevo estado.");
        }
        Reserva reserva = obtener(id);
        reserva.setEstado(estado);
        return repository.save(reserva);
    }

    private LocalDate parsearFecha(String texto) {
        if (texto == null || texto.isBlank()) {
            throw error("Ingresa la fecha del evento.");
        }
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            throw error("La fecha no es válida (usa aaaa-mm-dd).");
        }
    }

    private ResponseStatusException error(String mensaje) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }
}