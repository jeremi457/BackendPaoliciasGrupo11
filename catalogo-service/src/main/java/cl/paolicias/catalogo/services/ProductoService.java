package cl.paolicias.catalogo.services;

import cl.paolicias.catalogo.dto.ActivoRequest;
import cl.paolicias.catalogo.dto.ProductoRequest;
import cl.paolicias.catalogo.entities.Producto;
import java.util.List;

public interface ProductoService {

    List<Producto> listarActivos();

    List<Producto> listarTodos();

    Producto obtener(Long id);

    Producto crear(ProductoRequest request);

    Producto actualizar(Long id, ProductoRequest request);

    Producto cambiarActivo(Long id, ActivoRequest request);
}
