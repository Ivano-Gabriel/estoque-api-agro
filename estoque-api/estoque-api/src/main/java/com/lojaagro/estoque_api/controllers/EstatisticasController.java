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

        // Como ainda não temos uma tabela de "Histórico de Patrimônio por Mês", 
        // vamos simular a curva do gráfico baseada no patrimônio atual para o visual não quebrar.
        // No futuro, conectaremos isso com a tabela de Transações!
        List<Map<String, Object>> grafico = Arrays.asList(
            Map.of("mes", "Jan", "patrimonio", patrimonio.multiply(new BigDecimal("0.40"))),
            Map.of("mes", "Fev", "patrimonio", patrimonio.multiply(new BigDecimal("0.55"))),
            Map.of("mes", "Mar", "patrimonio", patrimonio.multiply(new BigDecimal("0.70"))),
            Map.of("mes", "Abr", "patrimonio", patrimonio.multiply(new BigDecimal("0.85"))),
            Map.of("mes", "Mai", "patrimonio", patrimonio.multiply(new BigDecimal("0.95"))),
            Map.of("mes", "Jun", "patrimonio", patrimonio)
        );
        stats.put("dadosGrafico", grafico);

        return ResponseEntity.ok(stats);
    }
}
