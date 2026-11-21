package cl.paolicias.pagos.controller;

import cl.paolicias.pagos.dto.PagoResponse;
import cl.paolicias.pagos.dto.ValidacionRequest;
import cl.paolicias.pagos.entities.Comprobante;
import cl.paolicias.pagos.services.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService service;

    @PostMapping(value = "/{reservaId}/comprobante", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PagoResponse subirComprobante(@PathVariable Long reservaId,
                                         @RequestParam("archivo") MultipartFile archivo) {
        return service.subirComprobante(reservaId, archivo);
    }

    @GetMapping("/{reservaId}/comprobante")
    public ResponseEntity<byte[]> obtenerComprobante(@PathVariable Long reservaId) {
        Comprobante comprobante = service.obtenerComprobante(reservaId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(comprobante.getContentType()))
                .body(comprobante.getDatos());
    }

    @PatchMapping("/{reservaId}/validacion")
    public PagoResponse validar(@PathVariable Long reservaId,
                                @RequestBody ValidacionRequest request) {
        return service.validar(reservaId, request.aprobado());
    }

    @GetMapping("/salud")
    public String salud() {
        return "pagos-service OK";
    }
}