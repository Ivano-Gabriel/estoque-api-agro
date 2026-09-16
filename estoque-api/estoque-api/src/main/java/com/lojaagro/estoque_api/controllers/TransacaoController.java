package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.entities.Transacao;
import com.lojaagro.estoque_api.services.TransacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService service;

    public TransacaoController(TransacaoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Transacao>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/ultimas")
    public ResponseEntity<List<Transacao>> listarUltimas10() {
        return ResponseEntity.ok(service.listarUltimas10());
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Transacao>> listarPorTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(service.listarPorTipo(tipo));
    }

    @GetMapping("/produto/{produtoId}")
    public ResponseEntity<List<Transacao>> listarPorProduto(@PathVariable Long produtoId) {
        return ResponseEntity.ok(service.listarPorProduto(produtoId));
    }
}
