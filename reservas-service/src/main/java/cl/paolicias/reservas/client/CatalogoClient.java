package cl.paolicias.reservas.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CatalogoClient {

    private final RestClient restClient;

    public CatalogoClient(@Value("${app.catalogo.url}") String url) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(5000);
        fabrica.setReadTimeout(10000);
        this.restClient = RestClient.builder()
                .baseUrl(url)
                .requestFactory(fabrica)
                .build();
    }

    public ProductoInfo obtenerProducto(Long id) {
        try {
            return restClient.get()
                    .uri("/productos/{id}", id)
                    .retrieve()
                    .body(ProductoInfo.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El producto no existe.");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El catálogo no está disponible. Intenta de nuevo en unos segundos.");
        }
    }
}