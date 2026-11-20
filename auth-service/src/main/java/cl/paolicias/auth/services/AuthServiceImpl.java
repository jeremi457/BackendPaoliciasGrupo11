package cl.paolicias.auth.services;

import cl.paolicias.auth.dto.LoginRequest;
import cl.paolicias.auth.dto.LoginResponse;
import cl.paolicias.auth.entities.Administrador;
import cl.paolicias.auth.repository.AdminRepository;
import cl.paolicias.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AdminRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {
        if (request.email() == null || request.email().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa correo y contraseña.");
        }

        Administrador admin = repository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos."));

        if (!passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos.");
        }

        String token = jwtService.generarToken(admin.getEmail(), "ADMIN");
        return new LoginResponse(token, admin.getNombre(), "ADMIN");
    }
}