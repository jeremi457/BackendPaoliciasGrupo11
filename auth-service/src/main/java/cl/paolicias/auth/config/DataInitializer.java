package cl.paolicias.auth.config;

import cl.paolicias.auth.entities.Administrador;
import cl.paolicias.auth.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String email;

    @Value("${app.admin.password}")
    private String password;

    @Value("${app.admin.nombre}")
    private String nombre;

    @Override
    public void run(String... args) {
        String correo = email.trim().toLowerCase();
        if (repository.findByEmail(correo).isEmpty()) {
            repository.save(new Administrador(null, correo, passwordEncoder.encode(password), nombre));
        }
    }
}