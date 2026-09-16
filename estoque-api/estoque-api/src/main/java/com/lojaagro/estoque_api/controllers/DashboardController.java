package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.entities.FluxoCaixa;
import com.lojaagro.estoque_api.entities.Transacao;
import com.lojaagro.estoque_api.services.FluxoCaixaService;
import com.lojaagro.estoque_api.services.ProdutoService;
import com.lojaagro.estoque_api.services.TransacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final FluxoCaixaService fluxoCaixaService;
    private final TransacaoService transacaoService;
    private final ProdutoService produtoService;

    public DashboardController(FluxoCaixaService fluxoCaixaService, 
                               TransacaoService transacaoService,
                               ProdutoService produtoService) {
        this.fluxoCaixaService = fluxoCaixaService;
        this.transacaoService = transacaoService;
        this.produtoService = produtoService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboard() {
        FluxoCaixa fluxo = fluxoCaixaService.getFluxoAtual();
        List<Transacao> ultimasTransacoes = transacaoService.listarUltimas10();
        BigDecimal totalVendas = transacaoService.somarPorTipo("VENDA");
        BigDecimal totalCompras = transacaoService.somarPorTipo("COMPRA");
        int totalProdutos = produtoService.buscarTodos().size();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("fluxoCaixa", fluxo);
        dashboard.put("ultimasTransacoes", ultimasTransacoes);
        dashboard.put("totalVendas", totalVendas);
        dashboard.put("totalCompras", totalCompras);
        dashboard.put("totalProdutosEstoque", totalProdutos);

        return ResponseEntity.ok(dashboard);
    }
}
