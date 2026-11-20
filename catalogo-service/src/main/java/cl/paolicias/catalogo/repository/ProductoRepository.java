package cl.paolicias.catalogo.repository;

import cl.paolicias.catalogo.entities.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrueOrderByIdAsc();

    List<Producto> findAllByOrderByIdAsc();
}
