package cl.paolicias.pagos.services;

import cl.paolicias.pagos.dto.PagoResponse;
import cl.paolicias.pagos.entities.Comprobante;
import org.springframework.web.multipart.MultipartFile;

public interface PagoService {

    PagoResponse subirComprobante(Long reservaId, MultipartFile archivo);

    Comprobante obtenerComprobante(Long reservaId);

    PagoResponse validar(Long reservaId, Boolean aprobado);
}