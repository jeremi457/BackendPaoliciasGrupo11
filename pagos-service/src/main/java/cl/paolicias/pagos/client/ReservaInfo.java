package cl.paolicias.pagos.client;

public record ReservaInfo(
        Long id,
        String clienteEmail,
        Long productoId,
        String productoNombre,
        String fecha,
        String horario,
        Integer asistentes,
        String telefono,
        String observaciones,
        Integer total,
        Integer abono,
        String estado,
        String creadaEn) {
}