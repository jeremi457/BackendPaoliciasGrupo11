package cl.paolicias.catalogo.controller;

import cl.paolicias.catalogo.dto.ActivoRequest;
import cl.paolicias.catalogo.dto.ProductoRequest;
import cl.paolicias.catalogo.entities.Producto;
import cl.paolicias.catalogo.services.ProductoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import cl.paolicias.catalogo.entities.ImagenProducto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService service;
    @GetMapping
    public List<Producto> listarActivos() {
        return service.listarActivos();
    }
    @GetMapping("/todos")
    public List<Producto> listarTodos() {
        return service.listarTodos();
    }
    @GetMapping("/salud")
    public String salud() {
        return "catalogo-service OK";
    }
    @GetMapping("/{id}")
    public Producto obtener(@PathVariable Long id) {
        return service.obtener(id);
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Producto crear(@RequestBody ProductoRequest request) {
        return service.crear(request);
    }
    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @RequestBody
    ProductoRequest request) {
        return service.actualizar(id, request);
    }
    @PatchMapping("/{id}/activo")
    public Producto cambiarActivo(@PathVariable Long id, @RequestBody
    ActivoRequest request) {
        return service.cambiarActivo(id, request);
    }

    @PostMapping(value = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Producto subirImagen(@PathVariable Long id,
                                @RequestParam("archivo") MultipartFile archivo) {
        return service.guardarImagen(id, archivo);
    }

    @GetMapping("/{id}/imagen")
    public ResponseEntity<byte[]> obtenerImagen(@PathVariable Long id) {
        ImagenProducto imagen = service.obtenerImagen(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagen.getContentType()))
                .body(imagen.getDatos());
    }
}
