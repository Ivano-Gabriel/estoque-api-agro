package com.lojaagro.estoque_api.config;

import com.lojaagro.estoque_api.controllers.AuthController;
import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.entities.UsuarioRole;
import com.lojaagro.estoque_api.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(-150)
public class PlatformAdminInitializer implements CommandLineRunner {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String email;
    private final String senha;

    public PlatformAdminInitializer(UsuarioRepository usuarios, PasswordEncoder encoder,
            @Value("${app.platform-admin.email:}") String email,
            @Value("${app.platform-admin.password:}") String senha) {
        this.usuarios = usuarios; this.encoder = encoder; this.email = email; this.senha = senha;
    }

    @Override
    public void run(String... args) {
        if (email.isBlank() && senha.isBlank()) return;
        if (email.isBlank() || senha.isBlank()) {
            throw new IllegalStateException("PLATFORM_ADMIN_EMAIL e PLATFORM_ADMIN_PASSWORD devem ser configurados juntos.");
        }
        AuthController.validarSenha(senha);
        String normalizado = email.trim().toLowerCase(java.util.Locale.ROOT);
        Usuario usuario = usuarios.findByEmail(normalizado).orElseGet(Usuario::new);
        if (usuario.getId() != null && usuario.getRole() != UsuarioRole.SUPER_ADMIN) {
            throw new IllegalStateException("PLATFORM_ADMIN_EMAIL já pertence a outro perfil.");
        }
        usuario.setEmail(normalizado);
        usuario.setRole(UsuarioRole.SUPER_ADMIN);
        usuario.setLoja(null);
        usuario.setAtivo(true);
        if (usuario.getId() == null || !encoder.matches(senha, usuario.getSenha())) {
            usuario.setSenha(encoder.encode(senha));
        }
        usuarios.save(usuario);
    }
}
