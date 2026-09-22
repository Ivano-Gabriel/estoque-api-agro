package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.dto.CriarUsuarioRequest;
import com.lojaagro.estoque_api.dto.LoginRequest;
import com.lojaagro.estoque_api.dto.LoginResponse;
import com.lojaagro.estoque_api.dto.LojaResumo;
import com.lojaagro.estoque_api.entities.UsuarioRole;
import com.lojaagro.estoque_api.repositories.UsuarioRepository;
import com.lojaagro.estoque_api.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioRepository repository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final com.lojaagro.estoque_api.security.LoginRateLimiter rateLimiter;
    private final String senhaInexistente;

    public AuthController(UsuarioRepository repository, 
                          JwtUtil jwtUtil, 
                          PasswordEncoder passwordEncoder,
                          com.lojaagro.estoque_api.security.LoginRateLimiter rateLimiter) {
        this.repository = repository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiter = rateLimiter;
        this.senhaInexistente = passwordEncoder.encode(java.util.UUID.randomUUID().toString());
    }

    @PostMapping("/registrar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> registrar(@Valid @RequestBody CriarUsuarioRequest request) {
        String email = request.email().trim().toLowerCase(java.util.Locale.ROOT);
        validarSenha(request.senha());
        if (request.role() != UsuarioRole.FUNCIONARIA) {
            throw new IllegalArgumentException("Administradores só podem cadastrar funcionárias.");
        }
        if (repository.existsByEmail(email)) {
            return ResponseEntity.status(409).body("Já existe um usuário com este e-mail.");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setRole(request.role());
        String adminEmail = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        Usuario admin = repository.findByEmail(adminEmail)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Sessão inválida."));
        usuario.setLoja(admin.getLoja());
        repository.save(usuario);
        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String email = request.email().trim().toLowerCase(java.util.Locale.ROOT);
        rateLimiter.verificar(email);
        Usuario usuario = repository.findByEmail(email).orElse(null);
        boolean confere = request.senha().getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72
                && passwordEncoder.matches(request.senha(), usuario == null ? senhaInexistente : usuario.getSenha());

        boolean lojaDisponivel = usuario != null && (usuario.getRole() == UsuarioRole.SUPER_ADMIN
                || usuario.getLoja() != null && usuario.getLoja().isAtiva());
        if (usuario == null || !usuario.isAtivo() || !lojaDisponivel || !confere) {
            return ResponseEntity.status(401).body("Email ou senha inválidos.");
        }

        return ResponseEntity.ok(new LoginResponse(
                jwtUtil.gerarToken(usuario),
                usuario.getEmail(),
                usuario.getRole(),
                LojaResumo.from(usuario.getLoja())));
    }
    public static void validarSenha(String senha) {
        if (senha == null || senha.length() < 10 || senha.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 10 caracteres e no máximo 72 bytes.");
        }
    }
}
