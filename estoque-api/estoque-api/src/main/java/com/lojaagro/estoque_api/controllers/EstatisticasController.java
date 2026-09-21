package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.repositories.ProdutoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@RestController
@RequestMapping("/estatisticas")
public class EstatisticasController {

    private final ProdutoRepository produtoRepository;

    public EstatisticasController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Puxa os dados reais do banco
        long ativos = produtoRepository.countByAtivoTrue();
        long criticos = produtoRepository.contarEstoqueCritico();
        BigDecimal patrimonio = produtoRepository.calcularPatrimonioTotal();

        stats.put("ativos", ativos);
        stats.put("criticos", criticos);
        stats.put("patrimonio", patrimonio);

        // Sem série histórica armazenada, não inventamos evolução mensal.
        stats.put("dadosGrafico", List.of());

        return ResponseEntity.ok(stats);
    }
}
