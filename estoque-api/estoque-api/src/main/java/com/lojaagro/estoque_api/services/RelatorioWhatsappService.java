package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.dto.RelatorioWhatsappResponse;
import com.lojaagro.estoque_api.entities.Produto;
import com.lojaagro.estoque_api.entities.Transacao;
import com.lojaagro.estoque_api.entities.Loja;
import com.lojaagro.estoque_api.repositories.ProdutoRepository;
import com.lojaagro.estoque_api.repositories.TransacaoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;

@Service
public class RelatorioWhatsappService {

    private static final DateTimeFormatter DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int LIMITE_PRODUTOS_CRITICOS = 10;

    private final TransacaoRepository transacaoRepository;
    private final ProdutoRepository produtoRepository;
    private final Clock businessClock;
    private final String numeroWhatsapp;

    public RelatorioWhatsappService(
            TransacaoRepository transacaoRepository,
            ProdutoRepository produtoRepository,
            Clock businessClock,
            @Value("${app.whatsapp.report-number:}") String numeroWhatsapp) {
        this.transacaoRepository = transacaoRepository;
        this.produtoRepository = produtoRepository;
        this.businessClock = businessClock;
        this.numeroWhatsapp = numeroWhatsapp;
    }

    public RelatorioWhatsappResponse gerar(String periodoInformado, Loja loja) {
        Periodo periodo = Periodo.from(periodoInformado);
        String numero = normalizarNumero(loja.getWhatsapp() == null ? numeroWhatsapp : loja.getWhatsapp());
        LocalDateTime agora = LocalDateTime.now(businessClock);
        LocalDateTime inicio = periodo.inicio(agora);

        List<Transacao> transacoes = transacaoRepository.findByPeriodo(loja.getId(), inicio, agora);
        List<Transacao> vendas = filtrar(transacoes, "VENDA");
        List<Transacao> compras = filtrar(transacoes, "COMPRA");
        List<Produto> produtosCriticos = produtoRepository.buscarEstoqueCritico(loja.getId());

        int unidadesVendidas = somarQuantidades(vendas);
        int unidadesRepostas = somarQuantidades(compras);
        BigDecimal totalVendido = somarValores(vendas);
        BigDecimal totalReposto = somarValores(compras);
        BigDecimal lucroReal = vendas.stream()
                .map(Transacao::getLucro)
                .map(this::zeroSeNulo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldo = totalVendido.subtract(totalReposto);

        String mensagem = montarMensagem(
                periodo, inicio, agora, vendas.size(), compras.size(),
                unidadesVendidas, unidadesRepostas, totalVendido,
                totalReposto, lucroReal, saldo, produtosCriticos, loja.isFinanceiroAtivo());
        String url = "https://wa.me/" + numero + "?text="
                + URLEncoder.encode(mensagem, StandardCharsets.UTF_8);

        return new RelatorioWhatsappResponse(periodo.name(), mensagem, url);
    }

    private List<Transacao> filtrar(List<Transacao> transacoes, String tipo) {
        return transacoes.stream()
                .filter(transacao -> tipo.equalsIgnoreCase(transacao.getTipo()))
                .toList();
    }

    private int somarQuantidades(List<Transacao> transacoes) {
        return transacoes.stream().mapToInt(Transacao::getQuantidade).sum();
    }

    private BigDecimal somarValores(List<Transacao> transacoes) {
        return transacoes.stream()
                .map(Transacao::getValorTotal)
                .map(this::zeroSeNulo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private String montarMensagem(
            Periodo periodo,
            LocalDateTime inicio,
            LocalDateTime agora,
            int quantidadeVendas,
            int quantidadeCompras,
            int unidadesVendidas,
            int unidadesRepostas,
            BigDecimal totalVendido,
            BigDecimal totalReposto,
            BigDecimal lucroReal,
            BigDecimal saldo,
            List<Produto> produtosCriticos,
            boolean financeiroAtivo) {

        StringBuilder mensagem = new StringBuilder();
        mensagem.append("*RELATÓRIO DE ESTOQUE*\n")
                .append("Período: ").append(periodo.rotulo)
                .append(" (até agora)\n")
                .append(inicio.format(DATA_HORA)).append(" a ")
                .append(agora.format(DATA_HORA)).append("\n\n")
                .append("*Movimentação*\n")
                .append("Vendas: ").append(quantidadeVendas)
                .append(' ').append(rotuloOperacoes(quantidadeVendas))
                .append(" / ").append(unidadesVendidas).append(' ')
                .append(rotuloUnidades(unidadesVendidas)).append("\n")
                .append("Reposições: ").append(quantidadeCompras)
                .append(' ').append(rotuloOperacoes(quantidadeCompras))
                .append(" / ").append(unidadesRepostas).append(' ')
                .append(rotuloUnidades(unidadesRepostas)).append("\n");

        if (financeiroAtivo) {
            mensagem.append("Total vendido: ").append(moeda(totalVendido)).append("\n")
                    .append("Total reposto: ").append(moeda(totalReposto)).append("\n")
                    .append("Lucro bruto das vendas: ").append(moeda(lucroReal)).append("\n")
                    .append("Saldo do período: ").append(moeda(saldo)).append("\n");
        }
        mensagem.append("\n*Estoque crítico agora: ")
                .append(produtosCriticos.size()).append("*\n");

        if (produtosCriticos.isEmpty()) {
            mensagem.append("Nenhum produto em estoque crítico.\n");
        } else {
            produtosCriticos.stream()
                    .limit(LIMITE_PRODUTOS_CRITICOS)
                    .forEach(produto -> mensagem.append("- ")
                            .append(produto.getNome())
                            .append(": ")
                            .append(produto.getQuantidadeEstoque())
                            .append(" un\n"));

            int restantes = produtosCriticos.size() - LIMITE_PRODUTOS_CRITICOS;
            if (restantes > 0) {
                mensagem.append("... e mais ").append(restantes).append(" produtos.\n");
            }
        }

        mensagem.append("\nGerado automaticamente pelo Estoque em ")
                .append(agora.format(DATA_HORA)).append('.');
        return mensagem.toString();
    }

    private String rotuloOperacoes(int quantidade) {
        return quantidade == 1 ? "operação" : "operações";
    }

    private String rotuloUnidades(int quantidade) {
        return quantidade == 1 ? "unidade" : "unidades";
    }

    private String moeda(BigDecimal valor) {
        return "R$ " + String.format(
                Locale.forLanguageTag("pt-BR"),
                "%,.2f",
                zeroSeNulo(valor).setScale(2, RoundingMode.HALF_UP));
    }

    private String normalizarNumero(String numeroInformado) {
        String numero = numeroInformado == null
                ? ""
                : numeroInformado.replaceAll("\\D", "");
        if (numero.length() < 10 || numero.length() > 15) {
            throw new IllegalArgumentException(
                    "Configure WHATSAPP_REPORT_NUMBER com DDI e DDD no servidor.");
        }
        return numero;
    }

    enum Periodo {
        DIARIO("Diário") {
            @Override
            LocalDateTime inicio(LocalDateTime agora) {
                return agora.toLocalDate().atStartOfDay();
            }
        },
        SEMANAL("Semanal") {
            @Override
            LocalDateTime inicio(LocalDateTime agora) {
                return agora.toLocalDate()
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        .atStartOfDay();
            }
        },
        MENSAL("Mensal") {
            @Override
            LocalDateTime inicio(LocalDateTime agora) {
                return agora.toLocalDate().withDayOfMonth(1).atStartOfDay();
            }
        };

        private final String rotulo;

        Periodo(String rotulo) {
            this.rotulo = rotulo;
        }

        abstract LocalDateTime inicio(LocalDateTime agora);

        static Periodo from(String valor) {
            if (valor == null || valor.isBlank()) {
                throw new IllegalArgumentException("Informe o periodo do relatorio.");
            }
            try {
                return valueOf(valor.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        "Periodo invalido. Use DIARIO, SEMANAL ou MENSAL.");
            }
        }
    }
}
