package com.lojaagro.estoque_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

public record MovimentacaoRequest(
        @Min(1) int quantidade,
        @DecimalMin(value = "0.01") double preco) {
}
