package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.repositories.ProdutoRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ferramentas destrutivas preservadas exclusivamente para testes locais.
 *
 * Este controller só é criado quando o profile "ferramentas-teste" é ativado
 * explicitamente. Nunca ative esse profile no Render ou em outro ambiente que
 * use dados reais de clientes.
 */
@RestController
@RequestMapping("/admin/ferramentas-teste")
@Profile("ferramentas-teste")
@PreAuthorize("hasRole('ADMIN')")
public class FerramentasTesteController {

    private final ProdutoRepository produtoRepository;
    private final JdbcTemplate jdbcTemplate;

    public FerramentasTesteController(ProdutoRepository produtoRepository,
                                      JdbcTemplate jdbcTemplate) {
        this.produtoRepository = produtoRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @DeleteMapping("/produtos/{id}/permanente")
    @Transactional
    public ResponseEntity<Void> deletarProdutoPermanente(@PathVariable Long id) {
        produtoRepository.apagarTransacoesDoProduto(id);
        produtoRepository.apagarPermanente(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reset-financeiro")
    @Transactional
    public ResponseEntity<Void> resetarFinanceiro() {
        jdbcTemplate.update("DELETE FROM transacao");
        jdbcTemplate.update("DELETE FROM fluxo_caixa");
        jdbcTemplate.update("""
                INSERT INTO fluxo_caixa
                    (id, total_entradas, total_saidas, saldo_liquido, versao)
                VALUES (1, 0, 0, 0, 0)
                """);
        return ResponseEntity.noContent().build();
    }
}
