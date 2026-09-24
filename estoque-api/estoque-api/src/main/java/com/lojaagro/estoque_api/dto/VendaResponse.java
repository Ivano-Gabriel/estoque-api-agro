package com.lojaagro.estoque_api.dto;

import com.lojaagro.estoque_api.entities.FormaPagamento;
import com.lojaagro.estoque_api.entities.StatusVenda;
import com.lojaagro.estoque_api.entities.Venda;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record VendaResponse(
        UUID id, String loja, String cliente, String telefoneCliente, String atendente,
        FormaPagamento formaPagamento, String formaPagamentoLabel,
        BigDecimal subtotal, BigDecimal desconto, BigDecimal total,
        BigDecimal valorRecebido, BigDecimal troco, StatusVenda status,
        LocalDateTime criadaEm, LocalDateTime canceladaEm, String motivoCancelamento,
        boolean exibirValores, List<Item> itens) {

    public record Item(Long produtoId, String nome, String tipo, int quantidade,
                       BigDecimal precoUnitario, BigDecimal subtotal,
                       BigDecimal desconto, BigDecimal total) {}

    public static VendaResponse de(Venda venda) {
        boolean financeiro = venda.getFormaPagamento() != FormaPagamento.NAO_INFORMADO;
        return new VendaResponse(venda.getId(), venda.getLoja().getNome(),
                venda.getCliente() == null ? null : venda.getCliente().getNome(),
                venda.getCliente() == null ? null : venda.getCliente().getTelefone(),
                venda.getUsuario().getEmail(), venda.getFormaPagamento(), venda.getFormaPagamento().getRotulo(),
                venda.getSubtotal(), venda.getDesconto(), venda.getTotal(), venda.getValorRecebido(),
                venda.getTroco(), venda.getStatus(), venda.getCriadaEm(), venda.getCanceladaEm(),
                venda.getMotivoCancelamento(), financeiro,
                venda.getItens().stream().map(item -> new Item(item.getProduto().getId(),
                        item.getNomeProduto(), item.getTipoProduto(), item.getQuantidade(),
                        item.getPrecoUnitario(), item.getSubtotal(), item.getDescontoRateado(),
                        item.getTotal())).toList());
    }
}
