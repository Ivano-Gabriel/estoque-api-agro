package com.lojaagro.estoque_api.entities;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FluxoCaixaTest {

    @Test
    void deveCalcularValoresMonetariosSemErroDePontoFlutuante() {
        FluxoCaixa fluxo = new FluxoCaixa();

        fluxo.adicionarEntrada(new BigDecimal("0.10"));
        fluxo.adicionarEntrada(new BigDecimal("0.20"));
        fluxo.adicionarSaida(new BigDecimal("0.10"));

        assertEquals(new BigDecimal("0.30"), fluxo.getTotalEntradas());
        assertEquals(new BigDecimal("0.10"), fluxo.getTotalSaidas());
        assertEquals(new BigDecimal("0.20"), fluxo.getSaldoLiquido());
    }
}
