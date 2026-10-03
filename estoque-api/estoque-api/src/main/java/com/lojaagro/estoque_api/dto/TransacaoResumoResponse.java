package com.lojaagro.estoque_api.dto;

import com.lojaagro.estoque_api.entities.FormaPagamento;
import com.lojaagro.estoque_api.entities.Transacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransacaoResumoResponse(
        Long id,
        String tipo,
        Produto produto,
        int quantidade,
        BigDecimal precoUnitario,
        BigDecimal valorTotal,
        BigDecimal custoUnitario,
        BigDecimal lucro,
        LocalDateTime data,
        String descricao,
        FormaPagamento formaPagamento,
        boolean estornada) {

    public record Produto(Long id, String nome) {}

    public static TransacaoResumoResponse de(Transacao transacao) {
        var produto = transacao.getProduto() == null
                ? null
                : new Produto(transacao.getProduto().getId(), transacao.getProduto().getNome());
        return new TransacaoResumoResponse(
                transacao.getId(), transacao.getTipo(), produto, transacao.getQuantidade(),
                transacao.getPrecoUnitario(), transacao.getValorTotal(),
                transacao.getCustoUnitario(), transacao.getLucro(), transacao.getData(),
                transacao.getDescricao(), transacao.getFormaPagamento(), transacao.isEstornada());
    }
}
