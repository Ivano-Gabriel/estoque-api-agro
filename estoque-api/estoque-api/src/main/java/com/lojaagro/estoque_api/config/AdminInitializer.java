package com.lojaagro.estoque_api.config;

import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.entities.UsuarioRole;
import com.lojaagro.estoque_api.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminInitializer(UsuarioRepository repository,
                            PasswordEncoder passwordEncoder,
                            @Value("${app.admin.email:}") String adminEmail,
                            @Value("${app.admin.password:}") String adminPassword) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (adminEmail.isBlank() && adminPassword.isBlank()) {
            return;
        }
        if (adminEmail.isBlank() || adminPassword.length() < 10) {
            throw new IllegalStateException("ADMIN_EMAIL e ADMIN_PASSWORD (mínimo 10 caracteres) devem ser configurados juntos");
        }

        String emailNormalizado = adminEmail.trim().toLowerCase();
        if (repository.existsByEmail(emailNormalizado)) {
            return;
        }

        Usuario admin = new Usuario();
        admin.setEmail(emailNormalizado);
        admin.setSenha(passwordEncoder.encode(adminPassword));
        admin.setRole(UsuarioRole.ADMIN);
        repository.save(admin);
    }
}
