package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.entities.UsuarioRole;
import com.lojaagro.estoque_api.repositories.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioAdminController {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    public UsuarioAdminController(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios; this.encoder = encoder;
    }
    public record Acesso(Long id, String email, boolean ativo) {}
    public record Estado(@NotNull Boolean ativo) {}
    public record Senha(@NotBlank @Size(min = 10, max = 72) String senha) {}

    @GetMapping
    public List<Acesso> listar() {
        return usuarios.findAll().stream().filter(u -> u.getRole() == UsuarioRole.FUNCIONARIA)
                .map(u -> new Acesso(u.getId(), u.getEmail(), u.isAtivo())).toList();
    }

    @PutMapping("/{id}/acesso")
    @Transactional
    public void alterarAcesso(@PathVariable Long id, @Valid @RequestBody Estado estado) {
        funcionaria(id).setAtivo(estado.ativo());
    }

    @PostMapping("/{id}/revogar-sessoes")
    @Transactional
    public void revogar(@PathVariable Long id) { funcionaria(id).revogarSessoes(); }

    @PutMapping("/{id}/senha")
    @Transactional
    public void trocarSenha(@PathVariable Long id, @Valid @RequestBody Senha senha) {
        AuthController.validarSenha(senha.senha());
        funcionaria(id).setSenha(encoder.encode(senha.senha()));
    }

    private Usuario funcionaria(Long id) {
        return usuarios.findById(id).filter(u -> u.getRole() == UsuarioRole.FUNCIONARIA)
                .orElseThrow(() -> new IllegalArgumentException("Funcionária não encontrada."));
    }
}
