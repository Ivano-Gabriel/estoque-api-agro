package com.lojaagro.estoque_api.dto;

import com.lojaagro.estoque_api.entities.FormaPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record VendaRequest(
        Long clienteId,
        @NotNull(message = "Informe a forma de pagamento.") FormaPagamento formaPagamento,
        @DecimalMin(value = "0.00", message = "O desconto não pode ser negativo.") BigDecimal desconto,
        @DecimalMin(value = "0.00", message = "O valor recebido não pode ser negativo.") BigDecimal valorRecebido,
        @NotEmpty(message = "Adicione pelo menos um produto à venda.")
        @Size(max = 100, message = "Uma venda pode ter no máximo 100 produtos diferentes.")
        List<@Valid Item> itens) {

    public record Item(
            @NotNull(message = "Produto é obrigatório.") Long produtoId,
            @Min(value = 1, message = "A quantidade deve ser no mínimo 1.")
            @Max(value = 1000000, message = "Quantidade acima do limite permitido.") int quantidade) {}
}
