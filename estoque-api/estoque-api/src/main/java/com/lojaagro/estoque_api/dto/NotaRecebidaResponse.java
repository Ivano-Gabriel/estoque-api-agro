package com.lojaagro.estoque_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record NotaRecebidaResponse(
        Long id, String fornecedor, String documentoFornecedor, String numero, String serie,
        String chaveAcesso, LocalDate dataEmissao, LocalDate dataRecebimento,
        BigDecimal valorTotal, boolean conferida, boolean estoqueAtualizado,
        String observacoes, LocalDateTime criadaEm, String cadastradaPor, List<Item> itens) {
    public record Item(Long produtoId, String produto, int quantidade, BigDecimal custoUnitario) {}
}
