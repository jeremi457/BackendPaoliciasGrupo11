package cl.paolicias.pagos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtVerifier {
    private final SecretKey clave;
    public JwtVerifier(@Value("${app.jwt.secret}") String secreto) {
        this.clave =
                Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }
    public Claims verificar(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
