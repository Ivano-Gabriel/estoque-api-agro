package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.entities.FluxoCaixa;
import com.lojaagro.estoque_api.services.FluxoCaixaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.lojaagro.estoque_api.services.UsuarioService;
import com.lojaagro.estoque_api.services.FinanceiroService;
import com.lojaagro.estoque_api.dto.CaixaResumoResponse;

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
    public ResponseEntity<CaixaResumoResponse> getFluxoAtual(Authentication auth) {
        var loja = usuarios.lojaAtual(auth);
        financeiro.exigirAtivo(loja);
        var caixa = service.getFluxoAtual(loja.getId());
        return ResponseEntity.ok(CaixaResumoResponse.de(caixa, service.recebimentosPorForma(loja.getId())));
    }
}
