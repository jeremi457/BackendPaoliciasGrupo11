package cl.paolicias.catalogo.repository;

import cl.paolicias.catalogo.entities.ImagenProducto;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImagenProductoRepository extends JpaRepository<ImagenProducto, Long> {

    Optional<ImagenProducto> findByProductoId(Long productoId);
}