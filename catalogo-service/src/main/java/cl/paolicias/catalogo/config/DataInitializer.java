package cl.paolicias.catalogo.config;

import cl.paolicias.catalogo.entities.Producto;
import cl.paolicias.catalogo.repository.ProductoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final ProductoRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            repository.saveAll(List.of(
                    new Producto(null, "Tortas personalizadas", 25000, "torta", true),
                    new Producto(null, "Tortas clásicas", 18000, "torta", true),
                    new Producto(null, "Cupcakes", 12000, "cup", true),
                    new Producto(null, "Mesa dulce", 45000, "mesa", true),
                    new Producto(null, "Coffee break", 35000, "coffe", true),
                    new Producto(null, "Menú de catering", 60000, "catering", true),
                    new Producto(null, "Galletas decoradas", 10000, "galleta", true),
                    new Producto(null, "Mini pasteles", 15000, "mini", true)
            ));
        }
    }
}
