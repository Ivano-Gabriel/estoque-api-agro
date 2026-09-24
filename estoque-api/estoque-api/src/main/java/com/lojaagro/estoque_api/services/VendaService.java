package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.dto.VendaRequest;
import com.lojaagro.estoque_api.dto.VendaResponse;
import com.lojaagro.estoque_api.entities.*;
import com.lojaagro.estoque_api.repositories.ProdutoRepository;
import com.lojaagro.estoque_api.repositories.TransacaoRepository;
import com.lojaagro.estoque_api.repositories.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VendaService {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final VendaRepository vendas;
    private final ProdutoRepository produtos;
    private final TransacaoRepository transacoes;
    private final TransacaoService transacaoService;
    private final FluxoCaixaService caixa;
    private final ClienteService clientes;
    private final Clock clock;

    public VendaService(VendaRepository vendas, ProdutoRepository produtos,
                        TransacaoRepository transacoes, TransacaoService transacaoService,
                        FluxoCaixaService caixa, ClienteService clientes, Clock clock) {
        this.vendas = vendas;
        this.produtos = produtos;
        this.transacoes = transacoes;
        this.transacaoService = transacaoService;
        this.caixa = caixa;
        this.clientes = clientes;
        this.clock = clock;
    }

    @Transactional
    public VendaResponse concluir(UUID chave, VendaRequest request, Usuario usuario) {
        Loja loja = exigirLoja(usuario);
        String assinatura = assinatura(request);
        Optional<Venda> repetida = vendas.findById(chave);
        if (repetida.isPresent()) {
            Venda anterior = repetida.get();
            if (!Objects.equals(anterior.getLoja().getId(), loja.getId())
                    || !MessageDigest.isEqual(anterior.getAssinatura().getBytes(StandardCharsets.UTF_8),
                                               assinatura.getBytes(StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("Esta chave já foi usada em outra venda.");
            }
            anterior.getItens().size();
            return VendaResponse.de(anterior);
        }

        caixa.bloquearOperacoes(loja.getId());
        // Outra requisição com a mesma chave pode ter concluído enquanto esta aguardava a trava.
        Optional<Venda> concluidaDuranteEspera = vendas.findById(chave);
        if (concluidaDuranteEspera.isPresent()) {
            Venda anterior = concluidaDuranteEspera.get();
            if (!Objects.equals(anterior.getLoja().getId(), loja.getId())
                    || !anterior.getAssinatura().equals(assinatura)) {
                throw new IllegalArgumentException("Esta chave já foi usada em outra venda.");
            }
            anterior.getItens().size();
            return VendaResponse.de(anterior);
        }
        Cliente cliente = clientes.entidadeOpcional(request.clienteId(), loja);
        validarItensUnicos(request.itens());

        List<Linha> linhas = new ArrayList<>();
        BigDecimal subtotal = ZERO;
        for (VendaRequest.Item solicitado : request.itens()) {
            Produto produto = produtos.findByIdAndLojaIdAndAtivoTrue(solicitado.produtoId(), loja.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado nesta loja."));
            if (produto.getQuantidadeEstoque() < solicitado.quantidade()) {
                throw new IllegalArgumentException("Estoque insuficiente para " + produto.getNome() + ".");
            }
            BigDecimal preco = normalizar(produto.getPreco());
            BigDecimal linhaSubtotal = preco.multiply(BigDecimal.valueOf(solicitado.quantidade())).setScale(2);
            linhas.add(new Linha(produto, solicitado.quantidade(), preco, linhaSubtotal));
            subtotal = subtotal.add(linhaSubtotal);
        }

        boolean financeiro = loja.isFinanceiroAtivo();
        FormaPagamento forma = financeiro ? request.formaPagamento() : FormaPagamento.NAO_INFORMADO;
        BigDecimal desconto = financeiro ? normalizar(request.desconto()) : ZERO;
        if (financeiro && (forma == null || forma == FormaPagamento.NAO_INFORMADO)) {
            throw new IllegalArgumentException("Selecione como o cliente pagou.");
        }
        if (financeiro && desconto.compareTo(subtotal) >= 0) {
            throw new IllegalArgumentException("O desconto deve ser menor que o subtotal da venda.");
        }
        BigDecimal total = financeiro ? subtotal.subtract(desconto).setScale(2) : ZERO;
        BigDecimal recebido = null;
        BigDecimal troco = null;
        if (financeiro && forma == FormaPagamento.DINHEIRO) {
            recebido = normalizar(request.valorRecebido());
            if (recebido.compareTo(total) < 0) {
                throw new IllegalArgumentException("O valor recebido é menor que o total da venda.");
            }
            troco = recebido.subtract(total).setScale(2);
        }

        Venda venda = new Venda(chave, loja, usuario, cliente, assinatura, forma,
                financeiro ? subtotal : ZERO, desconto, total, recebido, troco, LocalDateTime.now(clock));
        BigDecimal descontoDistribuido = ZERO;
        for (int indice = 0; indice < linhas.size(); indice++) {
            Linha linha = linhas.get(indice);
            BigDecimal rateio = !financeiro ? ZERO : indice == linhas.size() - 1
                    ? desconto.subtract(descontoDistribuido).setScale(2)
                    : desconto.multiply(linha.subtotal()).divide(subtotal, 2, RoundingMode.HALF_UP);
            descontoDistribuido = descontoDistribuido.add(rateio);
            BigDecimal totalLinha = financeiro ? linha.subtotal().subtract(rateio).setScale(2) : ZERO;
            venda.adicionarItem(new VendaItem(venda, linha.produto(), linha.quantidade(),
                    financeiro ? linha.preco() : ZERO, linha.produto().getCustoMedio(),
                    financeiro ? linha.subtotal() : ZERO, rateio, totalLinha));
        }
        vendas.saveAndFlush(venda);

        for (VendaItem item : venda.getItens()) {
            item.getProduto().venderProduto(item.getQuantidade());
            BigDecimal custoTotal = item.getCustoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()));
            BigDecimal lucro = financeiro ? item.getTotal().subtract(custoTotal).setScale(2) : ZERO;
            transacaoService.registrarTransacao(item.getProduto(), usuario, "VENDA", item.getQuantidade(),
                    item.getPrecoUnitario(), item.getTotal(), item.getCustoUnitario(), lucro,
                    "Venda PDV " + venda.getId(), cliente, venda, forma);
        }
        if (financeiro) caixa.adicionarEntrada(loja.getId(), total);
        return VendaResponse.de(venda);
    }

    @Transactional(readOnly = true)
    public List<VendaResponse> listar(Loja loja) {
        return vendas.findTop50ByLojaIdOrderByCriadaEmDesc(loja.getId()).stream()
                .map(VendaResponse::de).toList();
    }

    @Transactional
    public VendaResponse cancelar(UUID id, String motivo, Usuario responsavel) {
        Loja loja = exigirLoja(responsavel);
        caixa.bloquearOperacoes(loja.getId());
        Venda venda = vendas.bloquearPorIdELoja(id, loja.getId())
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada nesta loja."));
        if (venda.getStatus() == StatusVenda.CANCELADA) {
            throw new IllegalArgumentException("Esta venda já foi cancelada.");
        }
        for (VendaItem item : venda.getItens()) {
            Produto produto = produtos.findByIdAndLojaId(item.getProduto().getId(), loja.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto da venda não foi encontrado."));
            produto.reporSemCusto(item.getQuantidade());
        }
        transacoes.findByVendaId(id).forEach(transacao -> transacao.setEstornada(true));
        if (venda.getTotal().compareTo(BigDecimal.ZERO) > 0) {
            caixa.estornarEntrada(loja.getId(), venda.getTotal());
        }
        venda.cancelar(responsavel, motivo, LocalDateTime.now(clock));
        return VendaResponse.de(venda);
    }

    private Loja exigirLoja(Usuario usuario) {
        if (usuario.getLoja() == null || !usuario.getLoja().isAtiva()) {
            throw new org.springframework.security.access.AccessDeniedException("Loja indisponível.");
        }
        return usuario.getLoja();
    }

    private void validarItensUnicos(List<VendaRequest.Item> itens) {
        Set<Long> ids = new HashSet<>();
        for (VendaRequest.Item item : itens) {
            if (!ids.add(item.produtoId())) {
                throw new IllegalArgumentException("Cada produto deve aparecer apenas uma vez no carrinho.");
            }
        }
    }

    private String assinatura(VendaRequest request) {
        try {
            String itens = request.itens().stream()
                    .map(item -> item.produtoId() + ":" + item.quantidade()).reduce((a, b) -> a + "," + b).orElse("");
            String valor = String.valueOf(request.clienteId()) + "|" + request.formaPagamento() + "|"
                    + normalizar(request.desconto()) + "|" + normalizar(request.valorRecebido()) + "|" + itens;
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponível.", ex);
        }
    }

    private BigDecimal normalizar(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }

    private record Linha(Produto produto, int quantidade, BigDecimal preco, BigDecimal subtotal) {}
}
