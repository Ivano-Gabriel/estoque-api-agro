package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.entities.FluxoCaixa;
import com.lojaagro.estoque_api.repositories.FluxoCaixaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import com.lojaagro.estoque_api.entities.FormaPagamento;
import com.lojaagro.estoque_api.repositories.TransacaoRepository;
import com.lojaagro.estoque_api.repositories.PagamentoVendaRepository;

@Service
public class FluxoCaixaService {

    private final FluxoCaixaRepository repository;
    private final TransacaoRepository transacoes;
    private final PagamentoVendaRepository pagamentos;
    private final com.lojaagro.estoque_api.repositories.DevolucaoVendaRepository devolucoes;

    public FluxoCaixaService(FluxoCaixaRepository repository, TransacaoRepository transacoes,
                             PagamentoVendaRepository pagamentos,
                             com.lojaagro.estoque_api.repositories.DevolucaoVendaRepository devolucoes) {
        this.repository = repository;
        this.transacoes = transacoes;
        this.pagamentos = pagamentos;
        this.devolucoes = devolucoes;
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

    @Transactional
    public FluxoCaixa estornarEntrada(Long lojaId, BigDecimal valor) {
        FluxoCaixa fluxo = getFluxoAtual(lojaId);
        fluxo.estornarEntrada(valor);
        return repository.save(fluxo);
    }

    public FluxoCaixa getFluxoAtual(Long lojaId) {
        return repository.findByLojaId(lojaId)
            .orElseThrow(() -> new IllegalStateException("Caixa não inicializado."));
    }

    public Map<String, BigDecimal> recebimentosPorForma(Long lojaId) {
        Map<String, BigDecimal> resultado = new LinkedHashMap<>();
        for (FormaPagamento forma : FormaPagamento.values()) resultado.put(forma.name(), BigDecimal.ZERO.setScale(2));
        pagamentos.somarPorForma(lojaId).forEach(linha -> {
            FormaPagamento forma = linha[0] == null ? FormaPagamento.NAO_INFORMADO : (FormaPagamento) linha[0];
            resultado.put(forma.name(), ((BigDecimal) linha[1]).setScale(2));
        });
        devolucoes.somarPorForma(lojaId).forEach(linha -> {
            FormaPagamento forma = (FormaPagamento) linha[0];
            resultado.merge(forma.name(), ((BigDecimal) linha[1]).negate().setScale(2), BigDecimal::add);
        });
        transacoes.somarVendasLegadasPorForma(lojaId).forEach(linha -> {
            FormaPagamento forma = linha[0] == null ? FormaPagamento.NAO_INFORMADO : (FormaPagamento) linha[0];
            resultado.merge(forma.name(), ((BigDecimal) linha[1]).setScale(2), BigDecimal::add);
        });
        return resultado;
    }

    @Transactional(readOnly = true)
    public BigDecimal lucroBruto(Long lojaId) {
        BigDecimal total = transacoes.sumLucroOperacional(lojaId);
        return total == null ? BigDecimal.ZERO.setScale(2) : total.setScale(2);
    }

    // Uma trava no banco, compartilhada entre instâncias, serializa as operações da loja.
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void bloquearOperacoes(Long lojaId) {
        repository.bloquearCaixa(lojaId)
                .orElseThrow(() -> new IllegalStateException("Caixa não inicializado."));
    }
}
