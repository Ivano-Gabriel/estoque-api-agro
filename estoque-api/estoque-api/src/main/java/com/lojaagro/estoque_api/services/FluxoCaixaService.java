package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.entities.FluxoCaixa;
import com.lojaagro.estoque_api.repositories.FluxoCaixaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class FluxoCaixaService {

    private final FluxoCaixaRepository repository;

    public FluxoCaixaService(FluxoCaixaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FluxoCaixa adicionarEntrada(Long lojaId, BigDecimal valor) {
        FluxoCaixa fluxo = getFluxoAtual(lojaId);
        fluxo.adicionarEntrada(valor);
        return repository.save(fluxo);
    }

    @Transactional
    public FluxoCaixa adicionarSaida(Long lojaId, BigDecimal valor) {
        FluxoCaixa fluxo = getFluxoAtual(lojaId);
        fluxo.adicionarSaida(valor);
        return repository.save(fluxo);
    }

    public FluxoCaixa getFluxoAtual(Long lojaId) {
        return repository.findByLojaId(lojaId)
            .orElseThrow(() -> new IllegalStateException("Caixa não inicializado."));
    }

    // Uma trava no banco, compartilhada entre instâncias, serializa as operações da loja.
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void bloquearOperacoes(Long lojaId) {
        repository.bloquearCaixa(lojaId)
                .orElseThrow(() -> new IllegalStateException("Caixa não inicializado."));
    }
}
