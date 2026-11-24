package cl.paolicias.catalogo.services;

import cl.paolicias.catalogo.dto.ActivoRequest;
import cl.paolicias.catalogo.dto.ProductoRequest;
import cl.paolicias.catalogo.entities.Producto;
import java.util.List;
import cl.paolicias.catalogo.entities.ImagenProducto;
import org.springframework.web.multipart.MultipartFile;

public interface ProductoService {

    List<Producto> listarActivos();

    List<Producto> listarTodos();

    Producto obtener(Long id);

    Producto crear(ProductoRequest request);

    Producto actualizar(Long id, ProductoRequest request);

    Producto cambiarActivo(Long id, ActivoRequest request);

    Producto guardarImagen(Long id, MultipartFile archivo);

    ImagenProducto obtenerImagen(Long id);
}
