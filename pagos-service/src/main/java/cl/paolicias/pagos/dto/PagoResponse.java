package cl.paolicias.pagos.dto;

public record PagoResponse(Long reservaId, Integer monto, String estado) {
}