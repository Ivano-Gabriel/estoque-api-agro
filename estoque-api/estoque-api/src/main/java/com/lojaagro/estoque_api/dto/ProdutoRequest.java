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
        @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal preco,
        @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal custoUnitario,
        LocalDate dataValidade,
        @Min(0) int quantidadeEstoque,
        @NotNull @Valid CategoriaRequest categoria,
        @Size(max = 500) String descricao,
        @Size(max = 500)
        @jakarta.validation.constraints.Pattern(regexp = "^https://res\\.cloudinary\\.com/.+", message = "A imagem deve vir do armazenamento autorizado.")
        String imagemUrl,
        @Size(max = 60) String sku,
        @Size(max = 50) @jakarta.validation.constraints.Pattern(
                regexp = "^$|[0-9A-Za-z._-]{3,50}$",
                message = "Código de barras deve usar apenas letras, números, ponto, traço ou sublinhado.") String codigoBarras,
        @Size(max = 120) String variacao,
        @Min(0) Integer estoqueMinimo) {

    /** Compatibilidade com clientes e testes anteriores à identificação comercial. */
    public ProdutoRequest(String nome, String tipo, BigDecimal preco, BigDecimal custoUnitario,
                          LocalDate dataValidade, int quantidadeEstoque, CategoriaRequest categoria,
                          String descricao, String imagemUrl) {
        this(nome, tipo, preco, custoUnitario, dataValidade, quantidadeEstoque, categoria,
                descricao, imagemUrl, null, null, null, 5);
    }

    public int estoqueMinimoSeguro() { return estoqueMinimo == null ? 5 : estoqueMinimo; }
}
