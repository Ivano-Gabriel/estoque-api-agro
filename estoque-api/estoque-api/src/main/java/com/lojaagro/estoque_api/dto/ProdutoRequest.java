package com.lojaagro.estoque_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProdutoRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 40) String tipo,
        @DecimalMin("0.01") double preco,
        LocalDate dataValidade,
        @Min(0) int quantidadeEstoque,
        @NotNull @Valid CategoriaRequest categoria) {
}
