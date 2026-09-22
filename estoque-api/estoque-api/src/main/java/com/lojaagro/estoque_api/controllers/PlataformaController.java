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
    public PlataformaController(LojaService lojas) { this.lojas = lojas; }
    public record Estado(@NotNull Boolean ativa) {}
    public record Configuracao(
            @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 100) String nome,
            @NotNull Boolean financeiroAtivo,
            Boolean fotosAtivas,
            @jakarta.validation.constraints.Size(max = 20) String whatsapp) {}

    @GetMapping public List<Loja> listar() { return lojas.listar(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Loja criar(@Valid @RequestBody CriarLojaRequest request) { return lojas.criar(request); }

    @PutMapping("/{id}/status")
    public Loja alterarStatus(@PathVariable Long id, @Valid @RequestBody Estado estado) {
        return lojas.alterarStatus(id, estado.ativa());
    }

    @PutMapping("/{id}/configuracao")
    public Loja configurar(@PathVariable Long id, @Valid @RequestBody Configuracao config) {
        return lojas.configurar(id, config.nome(), config.financeiroAtivo(), config.fotosAtivas(), config.whatsapp());
    }
}
