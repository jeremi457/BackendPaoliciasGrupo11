package cl.paolicias.reservas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {

    private final JwtVerifier jwtVerifier;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String cabecera = request.getHeader("Authorization");
        if (cabecera == null || !cabecera.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Falta iniciar sesión como administrador.");
        }

        Claims claims;
        try {
            claims = jwtVerifier.verificar(cabecera.substring(7));
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "La sesión expiró o no es válida.");
        }

        if (!"ADMIN".equals(claims.get("rol", String.class))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Solo el administrador puede hacer esto.");
        }
        return true;
    }
}