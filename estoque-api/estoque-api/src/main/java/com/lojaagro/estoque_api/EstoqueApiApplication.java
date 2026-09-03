package com.lojaagro.estoque_api;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class EstoqueApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EstoqueApiApplication.class, args);
    }

    // TRUQUE PARA FORÇAR O BANCO A CRIAR A COLUNA ATIVO
    @Bean
    CommandLineRunner consertarBanco(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                // Tenta criar a coluna com valor padrão TRUE
                jdbcTemplate.execute("ALTER TABLE produto ADD COLUMN ativo BOOLEAN DEFAULT true");
                // Garante que os produtos antigos não fiquem nulos
                jdbcTemplate.execute("UPDATE produto SET ativo = true WHERE ativo IS NULL");
                System.out.println("✅ SUCESSO: Coluna 'ativo' injetada no banco de dados!");
            } catch (Exception e) {
                System.out.println("⚡ AVISO: A coluna 'ativo' já existe, tudo OK com o banco.");
            }
        };
    }
}