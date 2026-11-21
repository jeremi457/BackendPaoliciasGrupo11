package cl.paolicias.pagos.client;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ReservasClient {

    private final RestClient restClient;
    private final String claveInterna;

    public ReservasClient(@Value("${app.reservas.url}") String url,
                          @Value("${app.internal-key}") String claveInterna) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(5000);
        fabrica.setReadTimeout(10000);
        this.restClient = RestClient.builder()
                .baseUrl(url)
                .requestFactory(fabrica)
                .build();
        this.claveInterna = claveInterna;
    }

    public ReservaInfo obtenerReserva(Long id) {
        try {
            return restClient.get()
                    .uri("/reservas/{id}", id)
                    .retrieve()
                    .body(ReservaInfo.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La reserva no existe.");
        } catch (RestClientException e) {
            throw noDisponible();
        }
    }

    public void cambiarEstado(Long id, String estado) {
        try {
            restClient.patch()
                    .uri("/reservas/{id}/estado", id)
                    .header("X-Internal-Key", claveInterna)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("estado", estado))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw noDisponible();
        }
    }

    private ResponseStatusException noDisponible() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio de reservas no está disponible. Intenta de nuevo en unos segundos.");
    }
}