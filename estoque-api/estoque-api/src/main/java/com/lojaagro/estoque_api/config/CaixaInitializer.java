package com.lojaagro.estoque_api.config;

import com.lojaagro.estoque_api.entities.FluxoCaixa;
import com.lojaagro.estoque_api.repositories.FluxoCaixaRepository;
import com.lojaagro.estoque_api.repositories.LojaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(-100)
public class CaixaInitializer implements CommandLineRunner {
    private final FluxoCaixaRepository repository;
    private final LojaRepository lojas;

    public CaixaInitializer(FluxoCaixaRepository repository, LojaRepository lojas) {
        this.repository = repository; this.lojas = lojas;
    }

    @Override
    public void run(String... args) {
        lojas.findAll().forEach(loja -> repository.findByLojaId(loja.getId())
                .orElseGet(() -> repository.saveAndFlush(new FluxoCaixa(loja.getId(), loja))));
    }
}
