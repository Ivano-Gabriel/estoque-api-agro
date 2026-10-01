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
import org.springframework.security.core.Authentication;
import com.lojaagro.estoque_api.services.UsuarioService;

@RestController
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioAdminController {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final UsuarioService usuarioService;
    private final com.lojaagro.estoque_api.services.AuditoriaService auditoria;
    public UsuarioAdminController(UsuarioRepository usuarios, PasswordEncoder encoder, UsuarioService usuarioService,
                                  com.lojaagro.estoque_api.services.AuditoriaService auditoria) {
        this.usuarios = usuarios; this.encoder = encoder; this.usuarioService = usuarioService; this.auditoria = auditoria;
    }
    public record Acesso(Long id, String email, boolean ativo) {}
    public record Estado(@NotNull Boolean ativo) {}
    public record Senha(@NotBlank @Size(min = 10, max = 72) String senha) {}

    @GetMapping
    public List<Acesso> listar(Authentication auth) {
        Long lojaId = usuarioService.lojaAtual(auth).getId();
        return usuarios.findByLojaIdAndRole(lojaId, UsuarioRole.FUNCIONARIA).stream()
                .map(u -> new Acesso(u.getId(), u.getEmail(), u.isAtivo())).toList();
    }

    @PutMapping("/{id}/acesso")
    @Transactional
    public void alterarAcesso(@PathVariable Long id, @Valid @RequestBody Estado estado, Authentication auth) {
        Usuario alvo=funcionaria(id, auth); alvo.setAtivo(estado.ativo());
        auditoria.registrar(usuarioService.atual(auth), estado.ativo()?"LIBERAR":"BLOQUEAR", "USUARIO", id, alvo.getEmail());
    }

    @PostMapping("/{id}/revogar-sessoes")
    @Transactional
    public void revogar(@PathVariable Long id, Authentication auth) { Usuario alvo=funcionaria(id,auth); alvo.revogarSessoes(); auditoria.registrar(usuarioService.atual(auth),"REVOGAR_SESSOES","USUARIO",id,alvo.getEmail()); }

    @PutMapping("/{id}/senha")
    @Transactional
    public void trocarSenha(@PathVariable Long id, @Valid @RequestBody Senha senha, Authentication auth) {
        AuthController.validarSenha(senha.senha());
        Usuario alvo=funcionaria(id, auth); alvo.setSenha(encoder.encode(senha.senha()));
        auditoria.registrar(usuarioService.atual(auth),"TROCAR_SENHA","USUARIO",id,alvo.getEmail());
    }

    private Usuario funcionaria(Long id, Authentication auth) {
        Long lojaId = usuarioService.lojaAtual(auth).getId();
        return usuarios.findByIdAndLojaId(id, lojaId).filter(u -> u.getRole() == UsuarioRole.FUNCIONARIA)
                .orElseThrow(() -> new IllegalArgumentException("Funcionária não encontrada."));
    }
}
