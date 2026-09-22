package com.lojaagro.estoque_api.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Migração compatível: vincula os registros single-tenant à primeira loja. */
@Component
@Order(-300)
public class LojaMigrationInitializer implements CommandLineRunner {
    private final JdbcTemplate jdbc;
    public LojaMigrationInitializer(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    @Transactional
    public void run(String... args) {
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM loja", Long.class);
        if (total == null || total == 0) {
            jdbc.update("INSERT INTO loja (nome, slug, ativa, financeiro_ativo, fotos_ativas, versao) VALUES (?, ?, true, true, false, 0)",
                    "Loja piloto", "loja-piloto");
        }
        Long lojaId = jdbc.queryForObject("SELECT id FROM loja ORDER BY id LIMIT 1", Long.class);
        for (String tabela : new String[]{"categoria", "produto", "transacao", "operacao_estoque", "fluxo_caixa"}) {
            jdbc.update("UPDATE " + tabela + " SET loja_id = ? WHERE loja_id IS NULL", lojaId);
        }
        jdbc.update("UPDATE usuarios SET loja_id = ? WHERE loja_id IS NULL AND role <> 'SUPER_ADMIN'", lojaId);
    }
}
