package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.dto.RelatorioWhatsappResponse;
import com.lojaagro.estoque_api.entities.Produto;
import com.lojaagro.estoque_api.entities.Transacao;
import com.lojaagro.estoque_api.repositories.ProdutoRepository;
import com.lojaagro.estoque_api.repositories.TransacaoRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RelatorioWhatsappServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Maceio");
    private static final Clock RELOGIO = Clock.fixed(
            Instant.parse("2026-09-18T15:30:00Z"), ZONA);

    @Test
    void deveGerarRelatorioDiarioComValoresEEstoqueCritico() {
        TransacaoRepository transacaoRepository = mock(TransacaoRepository.class);
        ProdutoRepository produtoRepository = mock(ProdutoRepository.class);
        LocalDateTime agora = LocalDateTime.of(2026, 9, 18, 12, 30);
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 18, 0, 0);

        Transacao venda = transacao("VENDA", 2, "150.00", "80.00");
        Transacao compra = transacao("COMPRA", 3, "100.00", "0.00");
        Produto critico = mock(Produto.class);
        when(critico.getNome()).thenReturn("Racao Premium");
        when(critico.getQuantidadeEstoque()).thenReturn(4);
        when(transacaoRepository.findByPeriodo(inicio, agora))
                .thenReturn(List.of(venda, compra));
        when(produtoRepository.buscarEstoqueCritico()).thenReturn(List.of(critico));

        RelatorioWhatsappService service = new RelatorioWhatsappService(
                transacaoRepository, produtoRepository, RELOGIO, "55 (82) 99999-9999");

        RelatorioWhatsappResponse resposta = service.gerar("diario");

        assertEquals("DIARIO", resposta.periodo());
        assertTrue(resposta.mensagem().contains("Vendas: 1 operação / 2 unidades"));
        assertTrue(resposta.mensagem().contains("Reposições: 1 operação / 3 unidades"));
        assertTrue(resposta.mensagem().contains("Total vendido: R$ 300,00"));
        assertTrue(resposta.mensagem().contains("Total reposto: R$ 300,00"));
        assertTrue(resposta.mensagem().contains("Lucro bruto das vendas: R$ 80,00"));
        assertTrue(resposta.mensagem().contains("Racao Premium: 4 un"));
        assertTrue(resposta.url().startsWith("https://wa.me/5582999999999?text="));
    }

    @Test
    void deveRejeitarPeriodoInvalido() {
        RelatorioWhatsappService service = new RelatorioWhatsappService(
                mock(TransacaoRepository.class),
                mock(ProdutoRepository.class),
                RELOGIO,
                "5582999999999");

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.gerar("anual"));

        assertTrue(erro.getMessage().contains("DIARIO, SEMANAL ou MENSAL"));
    }

    private Transacao transacao(String tipo, int quantidade, String preco, String lucro) {
        Transacao transacao = new Transacao();
        transacao.setTipo(tipo);
        transacao.setQuantidade(quantidade);
        transacao.setPrecoUnitario(new BigDecimal(preco));
        transacao.setValorTotal(new BigDecimal(preco)
                .multiply(BigDecimal.valueOf(quantidade)));
        transacao.setCustoUnitario(BigDecimal.ZERO);
        transacao.setLucro(new BigDecimal(lucro));
        transacao.setData(LocalDateTime.now(RELOGIO));
        return transacao;
    }
}
