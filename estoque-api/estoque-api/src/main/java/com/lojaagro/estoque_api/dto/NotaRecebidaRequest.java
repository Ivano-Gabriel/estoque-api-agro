package com.lojaagro.estoque_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record NotaRecebidaRequest(
        @NotBlank @Size(max = 120) String fornecedor,
        @Size(max = 20) String documentoFornecedor,
        @NotBlank @Size(max = 30) String numero,
        @Size(max = 10) String serie,
        @Size(max = 60) String chaveAcesso,
        LocalDate dataEmissao,
        @NotNull LocalDate dataRecebimento,
        @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal valorTotal,
        boolean conferida,
        boolean atualizarEstoque,
        @Size(max = 1000) String observacoes,
        @Size(max = 500) List<@Valid Item> itens) {

    public record Item(
            @NotNull Long produtoId,
            @Min(1) int quantidade,
            @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal custoUnitario) {}
}
