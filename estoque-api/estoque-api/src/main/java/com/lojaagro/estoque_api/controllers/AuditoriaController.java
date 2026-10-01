package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.dto.AuditoriaResponse;
import com.lojaagro.estoque_api.services.AuditoriaService;
import com.lojaagro.estoque_api.services.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auditoria")
@PreAuthorize("hasRole('ADMIN')")
public class AuditoriaController {
    private final AuditoriaService auditoria;
    private final UsuarioService usuarios;

    public AuditoriaController(AuditoriaService auditoria, UsuarioService usuarios) {
        this.auditoria = auditoria;
        this.usuarios = usuarios;
    }

    @GetMapping
    public Page<AuditoriaResponse> listar(@RequestParam(defaultValue = "0") int pagina,
                                          @RequestParam(defaultValue = "30") int tamanho,
                                          Authentication auth) {
        return auditoria.listar(usuarios.lojaAtual(auth), pagina, tamanho);
    }
}
