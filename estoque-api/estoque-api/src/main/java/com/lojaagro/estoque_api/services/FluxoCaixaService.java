package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.entities.FluxoCaixa;
import com.lojaagro.estoque_api.repositories.FluxoCaixaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class FluxoCaixaService {

    private final FluxoCaixaRepository repository;
    private static final Long FLUXO_ID = 1L;

    public FluxoCaixaService(FluxoCaixaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FluxoCaixa adicionarEntrada(BigDecimal valor) {
        FluxoCaixa fluxo = getOrCreateFluxo();
        fluxo.adicionarEntrada(valor);
        return repository.save(fluxo);
    }

    @Transactional
    public FluxoCaixa adicionarSaida(BigDecimal valor) {
        FluxoCaixa fluxo = getOrCreateFluxo();
        fluxo.adicionarSaida(valor);
        return repository.save(fluxo);
    }

    public FluxoCaixa getFluxoAtual() {
        return getOrCreateFluxo();
    }

    private FluxoCaixa getOrCreateFluxo() {
        return repository.findById(FLUXO_ID)
            .orElseThrow(() -> new IllegalStateException("Caixa não inicializado."));
    }

    // Uma trava no banco, compartilhada entre instâncias, serializa as operações da loja.
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void bloquearOperacoes() {
        repository.bloquearCaixa()
                .orElseThrow(() -> new IllegalStateException("Caixa não inicializado."));
    }
}
