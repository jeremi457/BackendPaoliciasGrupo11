package cl.paolicias.pagos.services;

import cl.paolicias.pagos.client.ReservaInfo;
import cl.paolicias.pagos.client.ReservasClient;
import cl.paolicias.pagos.dto.PagoResponse;
import cl.paolicias.pagos.entities.Comprobante;
import cl.paolicias.pagos.entities.EstadoPago;
import cl.paolicias.pagos.entities.Pago;
import cl.paolicias.pagos.repository.ComprobanteRepository;
import cl.paolicias.pagos.repository.PagoRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final ZoneId ZONA = ZoneId.of("America/Santiago");

    private final PagoRepository pagoRepository;
    private final ComprobanteRepository comprobanteRepository;
    private final ReservasClient reservasClient;

    @Override
    @Transactional
    public PagoResponse subirComprobante(Long reservaId, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw error("Selecciona la imagen del comprobante.");
        }
        if (archivo.getSize() > MAX_BYTES) {
            throw error("La imagen supera los 5 MB.");
        }
        String tipo = archivo.getContentType();
        if (tipo == null || !(tipo.equals("image/jpeg") || tipo.equals("image/png"))) {
            throw error("El comprobante debe ser una imagen JPG o PNG.");
        }

        ReservaInfo reserva = reservasClient.obtenerReserva(reservaId);
        if ("CONFIRMADA".equals(reserva.estado())) {
            throw error("Esta reserva ya está confirmada.");
        }

        byte[] datos;
        try {
            datos = archivo.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo leer la imagen.");
        }

        // Si ya había un comprobante (por ejemplo, tras un rechazo), se reemplaza
        Comprobante comprobante = comprobanteRepository.findByReservaId(reservaId)
                .orElseGet(Comprobante::new);
        comprobante.setReservaId(reservaId);
        comprobante.setContentType(tipo);
        comprobante.setDatos(datos);
        comprobanteRepository.save(comprobante);

        Pago pago = pagoRepository.findByReservaId(reservaId).orElseGet(Pago::new);
        pago.setReservaId(reservaId);
        pago.setMonto(reserva.abono());
        pago.setEstado(EstadoPago.ENVIADO);
        pago.setEnviadoEn(LocalDateTime.now(ZONA));
        pago.setValidadoEn(null);
        pagoRepository.save(pago);

        reservasClient.cambiarEstado(reservaId, "COMPROBANTE_ENVIADO");
        return new PagoResponse(reservaId, pago.getMonto(), pago.getEstado().name());
    }

    @Override
    public Comprobante obtenerComprobante(Long reservaId) {
        return comprobanteRepository.findByReservaId(reservaId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aún no hay comprobante."));
    }

    @Override
    @Transactional
    public PagoResponse validar(Long reservaId, Boolean aprobado) {
        if (aprobado == null) {
            throw error("Indica si el pago se aprueba o se rechaza.");
        }
        Pago pago = pagoRepository.findByReservaId(reservaId).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay un pago para esta reserva."));
        if (pago.getEstado() != EstadoPago.ENVIADO) {
            throw error("Este pago ya fue revisado.");
        }

        pago.setEstado(aprobado ? EstadoPago.APROBADO : EstadoPago.RECHAZADO);
        pago.setValidadoEn(LocalDateTime.now(ZONA));
        pagoRepository.save(pago);

        reservasClient.cambiarEstado(reservaId, aprobado ? "CONFIRMADA" : "RECHAZADA");
        return new PagoResponse(reservaId, pago.getMonto(), pago.getEstado().name());
    }

    private ResponseStatusException error(String mensaje) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }
}