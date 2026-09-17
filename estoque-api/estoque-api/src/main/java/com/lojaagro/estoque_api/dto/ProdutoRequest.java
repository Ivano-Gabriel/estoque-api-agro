package com.lojaagro.estoque_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 40) String tipo,
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal preco,
        @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal custoUnitario,
        LocalDate dataValidade,
        @Min(0) int quantidadeEstoque,
        @NotNull @Valid CategoriaRequest categoria) {
}
