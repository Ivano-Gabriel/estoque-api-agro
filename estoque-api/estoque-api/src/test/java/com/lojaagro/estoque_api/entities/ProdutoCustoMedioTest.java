package com.lojaagro.estoque_api.entities;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProdutoCustoMedioTest {

    @Test
    void deveRecalcularCustoMedioPonderadoAoReporEstoque() {
        Produto produto = new Produto();
        produto.atualizarDados(
                "Produto teste", "UNIDADE", new BigDecimal("30.00"),
                null, new Categoria("Teste"));
        produto.inicializarEstoque(10, new BigDecimal("10.00"));

        produto.comprarProduto(10, new BigDecimal("20.00"));

        assertEquals(20, produto.getQuantidadeEstoque());
        assertEquals(new BigDecimal("15.00"), produto.getCustoMedio());
    }

    @Test
    void vendaNaoDeveAlterarCustoMedio() {
        Produto produto = new Produto();
        produto.atualizarDados(
                "Produto teste", "UNIDADE", new BigDecimal("30.00"),
                null, new Categoria("Teste"));
        produto.inicializarEstoque(5, new BigDecimal("12.50"));

        produto.venderProduto(2);

        assertEquals(3, produto.getQuantidadeEstoque());
        assertEquals(new BigDecimal("12.50"), produto.getCustoMedio());
    }
}
