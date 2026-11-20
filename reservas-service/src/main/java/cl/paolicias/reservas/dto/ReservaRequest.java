package cl.paolicias.reservas.dto;

public record ReservaRequest(
        String clienteEmail,
        Long productoId,
        String fecha,
        String horario,
        Integer asistentes,
        String telefono,
        String observaciones) {
}