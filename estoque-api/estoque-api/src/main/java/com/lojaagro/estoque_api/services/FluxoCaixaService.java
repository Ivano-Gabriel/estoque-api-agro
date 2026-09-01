package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.entities.FluxoCaixa;
import com.lojaagro.estoque_api.repositories.FluxoCaixaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FluxoCaixaService {

    private final FluxoCaixaRepository repository;
    private static final Long FLUXO_ID = 1L;

    public FluxoCaixaService(FluxoCaixaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FluxoCaixa adicionarEntrada(double valor) {
        FluxoCaixa fluxo = getOrCreateFluxo();
        fluxo.adicionarEntrada(valor);
        return repository.save(fluxo);
    }

    @Transactional
    public FluxoCaixa adicionarSaida(double valor) {
        FluxoCaixa fluxo = getOrCreateFluxo();
        fluxo.adicionarSaida(valor);
        return repository.save(fluxo);
    }

    public FluxoCaixa getFluxoAtual() {
        return getOrCreateFluxo();
    }

    private FluxoCaixa getOrCreateFluxo() {
        return repository.findById(FLUXO_ID)
            .orElseGet(() -> repository.save(new FluxoCaixa()));
    }
}