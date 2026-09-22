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
import org.springframework.security.core.Authentication;
import com.lojaagro.estoque_api.services.UsuarioService;
import com.lojaagro.estoque_api.services.FinanceiroService;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final FluxoCaixaService fluxoCaixaService;
    private final TransacaoService transacaoService;
    private final ProdutoService produtoService;
    private final UsuarioService usuarios;
    private final FinanceiroService financeiro;

    public DashboardController(FluxoCaixaService fluxoCaixaService, 
                               TransacaoService transacaoService,
                               ProdutoService produtoService, UsuarioService usuarios, FinanceiroService financeiro) {
        this.fluxoCaixaService = fluxoCaixaService;
        this.transacaoService = transacaoService;
        this.produtoService = produtoService;
        this.usuarios = usuarios; this.financeiro = financeiro;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboard(Authentication auth) {
        var loja = usuarios.lojaAtual(auth); financeiro.exigirAtivo(loja); Long lojaId = loja.getId();
        FluxoCaixa fluxo = fluxoCaixaService.getFluxoAtual(lojaId);
        List<Transacao> ultimasTransacoes = transacaoService.listarUltimas10(lojaId);
        BigDecimal totalVendas = transacaoService.somarPorTipo(lojaId, "VENDA");
        BigDecimal totalCompras = transacaoService.somarPorTipo(lojaId, "COMPRA");
        BigDecimal lucroReal = transacaoService.somarLucroReal(lojaId);
        int totalProdutos = produtoService.buscarTodos(loja).size();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("fluxoCaixa", fluxo);
        dashboard.put("ultimasTransacoes", ultimasTransacoes);
        dashboard.put("totalVendas", totalVendas);
        dashboard.put("totalCompras", totalCompras);
        dashboard.put("lucroReal", lucroReal);
        dashboard.put("totalProdutosEstoque", totalProdutos);

        return ResponseEntity.ok(dashboard);
    }
}
