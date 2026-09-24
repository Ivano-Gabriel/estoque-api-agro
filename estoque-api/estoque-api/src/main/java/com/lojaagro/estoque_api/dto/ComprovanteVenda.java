package com.lojaagro.estoque_api.dto;

import com.lojaagro.estoque_api.entities.Transacao;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ComprovanteVenda(
        Long transacaoId,
        String loja,
        String produto,
        int quantidade,
        BigDecimal precoUnitario,
        BigDecimal valorTotal,
        String cliente,
        String telefoneCliente,
        String atendente,
        LocalDateTime data,
        boolean exibirValores,
        String avisoFiscal) {

    public static ComprovanteVenda from(Transacao transacao) {
        return new ComprovanteVenda(
                transacao.getId(),
                transacao.getLoja().getNome(),
                transacao.getProduto().getNome(),
                transacao.getQuantidade(),
                transacao.getPrecoUnitario(),
                transacao.getValorTotal(),
                transacao.getCliente() == null ? null : transacao.getCliente().getNome(),
                transacao.getCliente() == null ? null : transacao.getCliente().getTelefone(),
                transacao.getUsuario().getEmail(),
                transacao.getData(),
                transacao.getLoja().isFinanceiroAtivo(),
                "COMPROVANTE NÃO FISCAL");
    }
}
