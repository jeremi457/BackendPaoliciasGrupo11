package cl.paolicias.catalogo.services;

import cl.paolicias.catalogo.dto.ActivoRequest;
import cl.paolicias.catalogo.dto.ProductoRequest;
import cl.paolicias.catalogo.entities.Producto;
import cl.paolicias.catalogo.repository.ProductoRepository;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {
    private static final Set<String> IMAGENES =
            Set.of("torta", "cup", "mesa", "coffe", "catering", "galleta",
                    "mini");
    private final ProductoRepository repository;
    @Override
    public List<Producto> listarActivos() {
        return repository.findByActivoTrueOrderByIdAsc();
    }

    @Override
    public List<Producto> listarTodos() {
        return repository.findAllByOrderByIdAsc();
    }

    @Override
    public Producto obtener(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado."));
    }

    @Override
    public Producto crear(ProductoRequest request) {
        validar(request);
        Producto producto = new Producto(
                null, request.nombre().trim(), request.precioBase(),
                request.imagen(), true);
        return repository.save(producto);
    }

    @Override
    public Producto actualizar(Long id, ProductoRequest request) {
        validar(request);
        Producto producto = obtener(id);
        producto.setNombre(request.nombre().trim());
        producto.setPrecioBase(request.precioBase());
        producto.setImagen(request.imagen());
        return repository.save(producto);
    }

    @Override
    public Producto cambiarActivo(Long id, ActivoRequest request) {
        if (request.activo() == null) {
            throw error("Indica si el producto está activo.");
        }
        Producto producto = obtener(id);
        producto.setActivo(request.activo());
        return repository.save(producto);
    }

    private void validar(ProductoRequest r) {
        if (r.nombre() == null || r.nombre().isBlank()) {
            throw error("Ingresa el nombre del producto.");
        }
        if (r.nombre().trim().length() > 40) {
            throw error("El nombre admite máximo 40 caracteres.");
        }
        if (r.precioBase() == null || r.precioBase() <= 0) {
            throw error("El precio debe ser mayor a 0.");
        }
        if (r.precioBase() > 9_999_999) {
            throw error("El precio es demasiado alto.");
        }
        if (r.imagen() == null || !IMAGENES.contains(r.imagen())) {
            throw error("La imagen no es válida.");
        }
    }

    private ResponseStatusException error(String mensaje) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }
}
