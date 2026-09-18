package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.dto.CriarUsuarioRequest;
import com.lojaagro.estoque_api.dto.LoginRequest;
import com.lojaagro.estoque_api.dto.LoginResponse;
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

    public AuthController(UsuarioRepository repository, 
                          JwtUtil jwtUtil, 
                          PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/registrar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> registrar(@Valid @RequestBody CriarUsuarioRequest request) {
        String email = request.email().trim().toLowerCase();
        if (repository.existsByEmail(email)) {
            return ResponseEntity.status(409).body("Já existe um usuário com este e-mail.");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setRole(request.role());
        repository.save(usuario);
        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        Usuario usuario = repository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(request.senha(), u.getSenha()))
                .orElse(null);

        if (usuario == null) {
            return ResponseEntity.status(401).body("Email ou senha inválidos.");
        }

        return ResponseEntity.ok(new LoginResponse(
                jwtUtil.gerarToken(usuario.getEmail()),
                usuario.getEmail(),
                usuario.getRole()));
    }
}
