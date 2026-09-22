package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.entities.Transacao;
import com.lojaagro.estoque_api.services.TransacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.security.core.Authentication;
import com.lojaagro.estoque_api.services.UsuarioService;
import com.lojaagro.estoque_api.services.FinanceiroService;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService service;
    private final UsuarioService usuarios;
    private final FinanceiroService financeiro;

    public TransacaoController(TransacaoService service, UsuarioService usuarios, FinanceiroService financeiro) {
        this.service = service; this.usuarios = usuarios; this.financeiro = financeiro;
    }

    private Long loja(Authentication auth) {
        var loja = usuarios.lojaAtual(auth); financeiro.exigirAtivo(loja); return loja.getId();
    }

    @GetMapping
    public ResponseEntity<List<Transacao>> listarTodas(Authentication auth) {
        return ResponseEntity.ok(service.listarTodas(loja(auth)));
    }

    @GetMapping("/ultimas")
    public ResponseEntity<List<Transacao>> listarUltimas10(Authentication auth) {
        return ResponseEntity.ok(service.listarUltimas10(loja(auth)));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Transacao>> listarPorTipo(@PathVariable String tipo, Authentication auth) {
        return ResponseEntity.ok(service.listarPorTipo(loja(auth), tipo));
    }

    @GetMapping("/produto/{produtoId}")
    public ResponseEntity<List<Transacao>> listarPorProduto(@PathVariable Long produtoId, Authentication auth) {
        return ResponseEntity.ok(service.listarPorProduto(loja(auth), produtoId));
    }
}
