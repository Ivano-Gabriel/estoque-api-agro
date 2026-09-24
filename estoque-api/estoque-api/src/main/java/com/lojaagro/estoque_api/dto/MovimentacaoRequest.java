package com.lojaagro.estoque_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MovimentacaoRequest(
        @Min(1) int quantidade,
        @DecimalMin(value = "0.00") @Digits(integer = 17, fraction = 2) BigDecimal preco,
        Long clienteId) {
    public MovimentacaoRequest(int quantidade, BigDecimal preco) {
        this(quantidade, preco, null);
    }
}
