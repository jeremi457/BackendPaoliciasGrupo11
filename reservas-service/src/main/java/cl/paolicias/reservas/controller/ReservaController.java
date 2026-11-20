package cl.paolicias.reservas.controller;

import cl.paolicias.reservas.dto.EstadoRequest;
import cl.paolicias.reservas.dto.ReservaRequest;
import cl.paolicias.reservas.entities.Reserva;
import cl.paolicias.reservas.services.ReservaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reserva crear(@RequestBody ReservaRequest request) {
        return service.crear(request);
    }

    @GetMapping
    public List<Reserva> listarPorEmail(@RequestParam String email) {
        return service.listarPorEmail(email);
    }

    @GetMapping("/todas")
    public List<Reserva> listarTodas() {
        return service.listarTodas();
    }

    @GetMapping("/salud")
    public String salud() {
        return "reservas-service OK";
    }

    @GetMapping("/{id}")
    public Reserva obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PatchMapping("/{id}/estado")
    public Reserva cambiarEstado(
            @PathVariable Long id,
            @RequestHeader(value = "X-Internal-Key", required = false) String claveInterna,
            @RequestBody EstadoRequest request) {
        return service.cambiarEstado(id, claveInterna, request.estado());
    }
}