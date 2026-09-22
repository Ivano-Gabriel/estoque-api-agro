package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.repositories.ProdutoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import org.springframework.security.core.Authentication;
import com.lojaagro.estoque_api.services.UsuarioService;

@RestController
@RequestMapping("/estatisticas")
public class EstatisticasController {

    private final ProdutoRepository produtoRepository;
    private final UsuarioService usuarios;

    public EstatisticasController(ProdutoRepository produtoRepository, UsuarioService usuarios) {
        this.produtoRepository = produtoRepository;
        this.usuarios = usuarios;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats(Authentication auth) {
        Map<String, Object> stats = new HashMap<>();

        var loja = usuarios.lojaAtual(auth);

        // Puxa os dados reais do banco
        long ativos = produtoRepository.countByLojaIdAndAtivoTrue(loja.getId());
        long criticos = produtoRepository.contarEstoqueCritico(loja.getId());
        BigDecimal patrimonio = loja.isFinanceiroAtivo()
                ? produtoRepository.calcularPatrimonioTotal(loja.getId()) : BigDecimal.ZERO;

        stats.put("ativos", ativos);
        stats.put("criticos", criticos);
        stats.put("patrimonio", patrimonio);

        // Sem série histórica armazenada, não inventamos evolução mensal.
        stats.put("dadosGrafico", List.of());

        return ResponseEntity.ok(stats);
    }
}
