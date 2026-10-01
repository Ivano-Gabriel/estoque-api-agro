package com.lojaagro.estoque_api.entities;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class CaixaSessaoTest {
    @Test void saldoFisicoConsideraSomenteDinheiroSangriaESuprimento() {
        Loja loja = new Loja("Loja", "loja", true, false, null);
        Usuario operador = new Usuario("caixa@loja.com", "hash");
        CaixaSessao caixa = new CaixaSessao(loja, operador, new BigDecimal("50.00"), LocalDateTime.now());
        caixa.registrarVenda(new BigDecimal("300.00"), new BigDecimal("80.00"));
        caixa.registrarSuprimento(new BigDecimal("20.00"));
        caixa.registrarSangria(new BigDecimal("10.00"));
        caixa.registrarEstorno(new BigDecimal("30.00"), new BigDecimal("5.00"));
        assertEquals(new BigDecimal("135.00"), caixa.saldoAtual());
        assertEquals(new BigDecimal("300.00"), caixa.getTotalVendas());
        assertEquals(new BigDecimal("80.00"), caixa.getTotalDinheiro());
    }

    @Test void fechamentoRegistraDiferencaENaoAceitaNovoMovimento() {
        CaixaSessao caixa = new CaixaSessao(new Loja("Loja", "loja", true, false, null),
                new Usuario("caixa@loja.com", "hash"), new BigDecimal("10.00"), LocalDateTime.now());
        caixa.fechar(new BigDecimal("9.00"), "Conferido", LocalDateTime.now());
        assertEquals(new BigDecimal("-1.00"), caixa.getDiferenca());
        assertThrows(IllegalArgumentException.class,
                () -> caixa.registrarVenda(BigDecimal.ONE, BigDecimal.ONE));
    }
}
