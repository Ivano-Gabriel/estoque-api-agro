package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.dto.CriarLojaRequest;
import com.lojaagro.estoque_api.entities.Loja;
import com.lojaagro.estoque_api.services.LojaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/plataforma/lojas")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class PlataformaController {
    private final LojaService lojas;
    private final com.lojaagro.estoque_api.services.UsuarioService usuarios;
    private final com.lojaagro.estoque_api.services.AuditoriaService auditoria;
    public PlataformaController(LojaService lojas, com.lojaagro.estoque_api.services.UsuarioService usuarios,
                                com.lojaagro.estoque_api.services.AuditoriaService auditoria) {
        this.lojas = lojas; this.usuarios = usuarios; this.auditoria = auditoria;
    }
    public record Estado(@NotNull Boolean ativa) {}
    public record Configuracao(
            @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 100) String nome,
            @NotNull Boolean financeiroAtivo,
            Boolean fotosAtivas,
            Boolean notasFiscaisAtivas,
            Boolean caixaOperacionalAtivo,
            Boolean lanchoneteAtiva,
            Boolean temaLanchoneteAtivo,
            @jakarta.validation.constraints.Size(max = 20) String whatsapp) {}

    @GetMapping public List<Loja> listar() { return lojas.listar(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Loja criar(@Valid @RequestBody CriarLojaRequest request, org.springframework.security.core.Authentication auth) {
        Loja loja=lojas.criar(request); auditoria.registrarPlataforma(usuarios.atual(auth),loja,"CRIAR","Nova loja: "+loja.getNome()); return loja;
    }

    @PutMapping("/{id}/status")
    public Loja alterarStatus(@PathVariable Long id, @Valid @RequestBody Estado estado, org.springframework.security.core.Authentication auth) {
        Loja loja=lojas.alterarStatus(id, estado.ativa()); auditoria.registrarPlataforma(usuarios.atual(auth),loja,estado.ativa()?"REATIVAR":"BLOQUEAR",null); return loja;
    }

    @PutMapping("/{id}/configuracao")
    public Loja configurar(@PathVariable Long id, @Valid @RequestBody Configuracao config, org.springframework.security.core.Authentication auth) {
        Loja loja=lojas.configurar(id, config.nome(), config.financeiroAtivo(), config.fotosAtivas(),
                config.notasFiscaisAtivas(), config.caixaOperacionalAtivo(),
                config.lanchoneteAtiva(), config.temaLanchoneteAtivo(), config.whatsapp());
        auditoria.registrarPlataforma(usuarios.atual(auth),loja,"CONFIGURAR","Módulos da loja atualizados"); return loja;
    }
}
