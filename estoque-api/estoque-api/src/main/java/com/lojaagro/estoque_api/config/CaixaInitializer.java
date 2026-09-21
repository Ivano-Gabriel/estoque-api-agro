package com.lojaagro.estoque_api.config;

import com.lojaagro.estoque_api.entities.FluxoCaixa;
import com.lojaagro.estoque_api.repositories.FluxoCaixaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(-100)
public class CaixaInitializer implements CommandLineRunner {
    private final FluxoCaixaRepository repository;

    public CaixaInitializer(FluxoCaixaRepository repository) { this.repository = repository; }

    @Override
    public void run(String... args) {
        if (repository.findAll().stream().anyMatch(caixa -> !Long.valueOf(1).equals(caixa.getId()))) {
            throw new IllegalStateException("Há caixas com ID diferente de 1. Faça backup e reconcilie os saldos antes de iniciar.");
        }
        if (!repository.existsById(1L)) {
            repository.saveAndFlush(new FluxoCaixa());
        }
    }
}
