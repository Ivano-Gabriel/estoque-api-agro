package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.entities.FluxoCaixa;
import com.lojaagro.estoque_api.services.FluxoCaixaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.lojaagro.estoque_api.services.UsuarioService;
import com.lojaagro.estoque_api.services.FinanceiroService;

@RestController
@RequestMapping("/fluxo-caixa")
public class FluxoCaixaController {

    private final FluxoCaixaService service;
    private final UsuarioService usuarios;
    private final FinanceiroService financeiro;

    public FluxoCaixaController(FluxoCaixaService service, UsuarioService usuarios, FinanceiroService financeiro) {
        this.service = service; this.usuarios = usuarios; this.financeiro = financeiro;
    }

    @GetMapping
    public ResponseEntity<FluxoCaixa> getFluxoAtual(Authentication auth) {
        var loja = usuarios.lojaAtual(auth);
        financeiro.exigirAtivo(loja);
        return ResponseEntity.ok(service.getFluxoAtual(loja.getId()));
    }
}
