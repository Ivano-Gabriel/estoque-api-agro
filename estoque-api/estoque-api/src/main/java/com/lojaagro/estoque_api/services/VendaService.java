package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.dto.VendaRequest;
import com.lojaagro.estoque_api.dto.VendaResponse;
import com.lojaagro.estoque_api.entities.*;
import com.lojaagro.estoque_api.repositories.ProdutoRepository;
import com.lojaagro.estoque_api.repositories.TransacaoRepository;
import com.lojaagro.estoque_api.repositories.VendaRepository;
import com.lojaagro.estoque_api.repositories.DevolucaoVendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
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
    private final CaixaOperacionalService caixaOperacional;
    private final AuditoriaService auditoria;
    private final DevolucaoVendaRepository devolucoes;
    private final com.lojaagro.estoque_api.repositories.PedidoLanchoneteRepository pedidosLanchonete;

    public VendaService(VendaRepository vendas, ProdutoRepository produtos,
                        TransacaoRepository transacoes, TransacaoService transacaoService,
                        FluxoCaixaService caixa, ClienteService clientes, Clock clock,
                        CaixaOperacionalService caixaOperacional, AuditoriaService auditoria,
                        DevolucaoVendaRepository devolucoes,
                        com.lojaagro.estoque_api.repositories.PedidoLanchoneteRepository pedidosLanchonete) {
        this.vendas = vendas;
        this.produtos = produtos;
        this.transacoes = transacoes;
        this.transacaoService = transacaoService;
        this.caixa = caixa;
        this.clientes = clientes;
        this.clock = clock;
        this.caixaOperacional = caixaOperacional;
        this.auditoria = auditoria;
        this.devolucoes = devolucoes;
        this.pedidosLanchonete = pedidosLanchonete;
    }

    @Transactional
    public VendaResponse concluir(UUID chave, VendaRequest request, Usuario usuario) {
        return concluirInterno(chave, request, usuario, Map.of());
    }

    @Transactional
    public VendaResponse concluirPedido(UUID chave, VendaRequest request, Usuario usuario,
                                        Map<Long, BigDecimal> custosConfiaveis) {
        return concluirInterno(chave, request, usuario,
                custosConfiaveis == null ? Map.of() : Map.copyOf(custosConfiaveis));
    }

    private VendaResponse concluirInterno(UUID chave, VendaRequest request, Usuario usuario,
                                          Map<Long, BigDecimal> custosConfiaveis) {
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
            if (produto.isControlaEstoque() && produto.getQuantidadeEstoque() < solicitado.quantidade()) {
                throw new IllegalArgumentException("Estoque insuficiente para " + produto.getNome() + ".");
            }
            BigDecimal preco = normalizar(produto.getPreco());
            BigDecimal linhaSubtotal = preco.multiply(BigDecimal.valueOf(solicitado.quantidade())).setScale(2);
            BigDecimal custo = normalizar(custosConfiaveis.getOrDefault(
                    produto.getId(), produto.getCustoMedio()));
            if (custo.signum() < 0) throw new IllegalArgumentException("Custo da venda não pode ser negativo.");
            linhas.add(new Linha(produto, solicitado.quantidade(), preco, custo, linhaSubtotal));
            subtotal = subtotal.add(linhaSubtotal);
        }

        boolean financeiro = loja.isFinanceiroAtivo();
        BigDecimal desconto = financeiro ? normalizar(request.desconto()) : ZERO;
        if (financeiro && desconto.compareTo(subtotal) >= 0) {
            throw new IllegalArgumentException("O desconto deve ser menor que o subtotal da venda.");
        }
        BigDecimal total = financeiro ? subtotal.subtract(desconto).setScale(2) : ZERO;
        List<PagamentoCalculado> pagamentos = calcularPagamentos(request, financeiro, total);
        FormaPagamento forma = !financeiro ? FormaPagamento.NAO_INFORMADO
                : pagamentos.size() == 1 ? pagamentos.getFirst().forma() : FormaPagamento.MULTIPLO;
        BigDecimal recebido = null;
        BigDecimal troco = null;
        BigDecimal parcelaDinheiro = pagamentos.stream().filter(p -> p.forma() == FormaPagamento.DINHEIRO)
                .map(PagamentoCalculado::valor).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
        if (financeiro && parcelaDinheiro.signum() > 0) {
            recebido = normalizar(request.valorRecebido());
            if (recebido.compareTo(parcelaDinheiro) < 0) {
                throw new IllegalArgumentException("O valor recebido é menor que a parte paga em dinheiro.");
            }
            troco = recebido.subtract(parcelaDinheiro).setScale(2);
        }

        Venda venda = new Venda(chave, loja, usuario, cliente, assinatura, forma,
                financeiro ? subtotal : ZERO, desconto, total, recebido, troco, LocalDateTime.now(clock));
        pagamentos.forEach(p -> venda.adicionarPagamento(new PagamentoVenda(venda, p.forma(), p.valor())));
        BigDecimal descontoDistribuido = ZERO;
        for (int indice = 0; indice < linhas.size(); indice++) {
            Linha linha = linhas.get(indice);
            BigDecimal rateio = !financeiro ? ZERO : indice == linhas.size() - 1
                    ? desconto.subtract(descontoDistribuido).setScale(2)
                    : desconto.multiply(linha.subtotal()).divide(subtotal, 2, RoundingMode.HALF_UP);
            descontoDistribuido = descontoDistribuido.add(rateio);
            BigDecimal totalLinha = financeiro ? linha.subtotal().subtract(rateio).setScale(2) : ZERO;
            venda.adicionarItem(new VendaItem(venda, linha.produto(), linha.quantidade(),
                    financeiro ? linha.preco() : ZERO, linha.custo(),
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
        if (financeiro) {
            caixa.adicionarEntrada(loja.getId(), total);
            caixaOperacional.registrarVenda(usuario, venda, total, parcelaDinheiro);
        }
        auditoria.registrar(usuario, "CONCLUIR", "VENDA", venda.getId(),
                "Itens: " + venda.getItens().size() + " • Total: " + total);
        return VendaResponse.de(venda);
    }

    @Transactional(readOnly = true)
    public List<VendaResponse> listar(Loja loja) {
        return vendas.findTop50ByLojaIdOrderByCriadaEmDesc(loja.getId()).stream()
                .map(VendaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public com.lojaagro.estoque_api.dto.PaginaResponse<VendaResponse> pesquisar(
            Loja loja, int pagina, int tamanho, LocalDateTime inicio, LocalDateTime fim,
            StatusVenda status, String busca) {
        int limite = Math.min(100, Math.max(1, tamanho));
        String termo = busca == null ? "" : busca.trim();
        Specification<Venda> filtros = (root, query, cb) -> {
            List<Predicate> criterios = new ArrayList<>();
            criterios.add(cb.equal(root.get("loja").get("id"), loja.getId()));
            if (inicio != null) criterios.add(cb.greaterThanOrEqualTo(root.get("criadaEm"), inicio));
            if (fim != null) criterios.add(cb.lessThanOrEqualTo(root.get("criadaEm"), fim));
            if (status != null) criterios.add(cb.equal(root.get("status"), status));
            if (!termo.isBlank()) {
                String nome = "%" + termo.toLowerCase(Locale.ROOT) + "%";
                var cliente = root.join("cliente", JoinType.LEFT);
                Predicate porNome = cb.like(cb.lower(cb.coalesce(
                        cliente.<String>get("nome"), "")), nome);
                try {
                    criterios.add(cb.or(porNome, cb.equal(root.get("id"), UUID.fromString(termo))));
                } catch (IllegalArgumentException ignorado) {
                    criterios.add(porNome);
                }
            }
            return cb.and(criterios.toArray(Predicate[]::new));
        };
        var paginaVendas = vendas.findAll(filtros, org.springframework.data.domain.PageRequest.of(
                Math.max(0, pagina), limite,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "criadaEm")));
        var paginaIds = paginaVendas.map(Venda::getId);
        List<Venda> detalhes = paginaIds.isEmpty()
                ? List.of() : vendas.buscarDetalhes(paginaIds.getContent());
        Map<UUID, Venda> porId = detalhes.stream()
                .collect(java.util.stream.Collectors.toMap(Venda::getId, v -> v));
        List<VendaResponse> conteudo = paginaIds.getContent().stream().map(porId::get)
                .filter(Objects::nonNull).map(VendaResponse::de).toList();
        return new com.lojaagro.estoque_api.dto.PaginaResponse<>(conteudo, paginaIds.getNumber(),
                paginaIds.getSize(), paginaIds.getTotalElements(), paginaIds.getTotalPages(), paginaIds.isLast());
    }

    @Transactional
    public VendaResponse cancelar(UUID id, String motivo, Usuario responsavel) {
        if (pedidosLanchonete.existsByVendaId(id)) {
            throw new IllegalArgumentException("Cancele esta venda pela tela de pedidos da lanchonete.");
        }
        return cancelarInterno(id, motivo, responsavel);
    }

    @Transactional
    public VendaResponse cancelarPedido(UUID id, String motivo, Usuario responsavel) {
        return cancelarInterno(id, motivo, responsavel);
    }

    private VendaResponse cancelarInterno(UUID id, String motivo, Usuario responsavel) {
        Loja loja = exigirLoja(responsavel);
        caixa.bloquearOperacoes(loja.getId());
        Venda venda = vendas.bloquearPorIdELoja(id, loja.getId())
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada nesta loja."));
        if (venda.getStatus() == StatusVenda.CANCELADA) {
            throw new IllegalArgumentException("Esta venda já foi cancelada.");
        }
        if (venda.getTotalDevolvido().signum() > 0) {
            throw new IllegalArgumentException("Venda com devolução não pode ser cancelada. Devolva apenas os itens restantes.");
        }
        for (VendaItem item : venda.getItens()) {
            Produto produto = produtos.findByIdAndLojaId(item.getProduto().getId(), loja.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto da venda não foi encontrado."));
            produto.reporSemCusto(item.getQuantidade());
        }
        transacoes.findByVendaId(id).forEach(transacao -> transacao.setEstornada(true));
        if (venda.getTotal().compareTo(BigDecimal.ZERO) > 0) {
            caixa.estornarEntrada(loja.getId(), venda.getTotal());
            BigDecimal dinheiro = venda.getPagamentos().stream()
                    .filter(p -> p.getForma() == FormaPagamento.DINHEIRO).map(PagamentoVenda::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
            caixaOperacional.registrarEstorno(responsavel, venda, venda.getTotal(), dinheiro);
        }
        venda.cancelar(responsavel, motivo, LocalDateTime.now(clock));
        auditoria.registrar(responsavel, "CANCELAR", "VENDA", venda.getId(),
                "Total estornado: " + venda.getTotal() + " • Motivo: " + motivo);
        return VendaResponse.de(venda);
    }

    @Transactional(readOnly = true)
    public Venda entidade(UUID id, Loja loja) {
        return vendas.findByIdAndLojaId(id, loja.getId())
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada nesta loja."));
    }

    @Transactional
    public com.lojaagro.estoque_api.dto.DevolucaoVendaResponse devolver(
            UUID vendaId, com.lojaagro.estoque_api.dto.DevolucaoVendaRequest request, Usuario responsavel) {
        Loja loja = exigirLoja(responsavel);
        caixa.bloquearOperacoes(loja.getId());
        Venda venda = vendas.bloquearPorIdELoja(vendaId, loja.getId())
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada nesta loja."));
        if (venda.getStatus() == StatusVenda.CANCELADA || venda.getStatus() == StatusVenda.DEVOLVIDA)
            throw new IllegalArgumentException("Esta venda não aceita novas devoluções.");
        if (request.formaReembolso() == FormaPagamento.NAO_INFORMADO
                || request.formaReembolso() == FormaPagamento.MULTIPLO)
            throw new IllegalArgumentException("Informe uma forma de reembolso válida.");
        Set<Long> ids = new HashSet<>();
        DevolucaoVenda devolucao = new DevolucaoVenda(loja, venda, responsavel,
                request.formaReembolso(), request.motivo(), LocalDateTime.now(clock));
        for (var solicitado : request.itens()) {
            if (!ids.add(solicitado.vendaItemId()))
                throw new IllegalArgumentException("Item repetido na devolução.");
            VendaItem item = venda.getItens().stream().filter(i -> i.getId().equals(solicitado.vendaItemId()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Item não pertence a esta venda."));
            BigDecimal valor = item.calcularDevolucao(solicitado.quantidade());
            Produto produto = produtos.findByIdAndLojaId(item.getProduto().getId(), loja.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto da venda não foi encontrado."));
            produto.reporSemCusto(solicitado.quantidade());
            item.registrarDevolucao(solicitado.quantidade(), valor);
            devolucao.adicionar(new DevolucaoVendaItem(devolucao, item, solicitado.quantidade(), valor));
            BigDecimal custo = item.getCustoUnitario().multiply(BigDecimal.valueOf(solicitado.quantidade())).setScale(2);
            BigDecimal lucroEstornado = valor.subtract(custo).negate().setScale(2);
            transacaoService.registrarTransacao(produto, responsavel, "DEVOLUCAO", solicitado.quantidade(),
                    valor.divide(BigDecimal.valueOf(solicitado.quantidade()), 2, RoundingMode.HALF_UP), valor,
                    item.getCustoUnitario(), lucroEstornado, "Devolução da venda " + venda.getId(),
                    venda.getCliente(), venda, request.formaReembolso());
        }
        if (devolucao.getValor().signum() <= 0) throw new IllegalArgumentException("Devolução sem valor.");
        venda.registrarDevolucao(devolucao.getValor());
        devolucoes.save(devolucao);
        caixa.estornarEntrada(loja.getId(), devolucao.getValor());
        caixaOperacional.registrarEstorno(responsavel, venda, devolucao.getValor(),
                request.formaReembolso() == FormaPagamento.DINHEIRO ? devolucao.getValor() : ZERO);
        auditoria.registrar(responsavel, "DEVOLVER", "VENDA", venda.getId(),
                "Valor: " + devolucao.getValor() + " • Motivo: " + request.motivo());
        return com.lojaagro.estoque_api.dto.DevolucaoVendaResponse.de(devolucao);
    }

    @Transactional(readOnly = true)
    public List<com.lojaagro.estoque_api.dto.DevolucaoVendaResponse> listarDevolucoes(UUID vendaId, Loja loja) {
        vendas.findByIdAndLojaId(vendaId, loja.getId())
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada nesta loja."));
        return devolucoes.findByVendaIdOrderByCriadaEmDesc(vendaId).stream()
                .map(com.lojaagro.estoque_api.dto.DevolucaoVendaResponse::de).toList();
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
            String pagamentos = request.pagamentos() == null ? String.valueOf(request.formaPagamento())
                    : request.pagamentos().stream().map(p -> p.forma() + ":" + normalizar(p.valor()))
                    .reduce((a, b) -> a + "," + b).orElse("");
            String valor = String.valueOf(request.clienteId()) + "|" + pagamentos + "|"
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

    private List<PagamentoCalculado> calcularPagamentos(VendaRequest request, boolean financeiro, BigDecimal total) {
        if (!financeiro) return List.of(new PagamentoCalculado(FormaPagamento.NAO_INFORMADO, ZERO));
        List<PagamentoCalculado> resultado = new ArrayList<>();
        if (request.pagamentos() != null && !request.pagamentos().isEmpty()) {
            Set<FormaPagamento> formas = new HashSet<>();
            for (VendaRequest.Pagamento pagamento : request.pagamentos()) {
                if (pagamento.forma() == FormaPagamento.NAO_INFORMADO || pagamento.forma() == FormaPagamento.MULTIPLO)
                    throw new IllegalArgumentException("Forma de pagamento inválida.");
                if (!formas.add(pagamento.forma()))
                    throw new IllegalArgumentException("Cada forma de pagamento deve aparecer apenas uma vez.");
                BigDecimal valor = normalizar(pagamento.valor());
                if (valor.signum() <= 0) throw new IllegalArgumentException("Pagamento deve ser maior que zero.");
                resultado.add(new PagamentoCalculado(pagamento.forma(), valor));
            }
        } else {
            FormaPagamento forma = request.formaPagamento();
            if (forma == null || forma == FormaPagamento.NAO_INFORMADO || forma == FormaPagamento.MULTIPLO)
                throw new IllegalArgumentException("Selecione como o cliente pagou.");
            resultado.add(new PagamentoCalculado(forma, total));
        }
        BigDecimal soma = resultado.stream().map(PagamentoCalculado::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
        if (soma.compareTo(total) != 0)
            throw new IllegalArgumentException("A soma dos pagamentos deve ser igual ao total da venda.");
        return List.copyOf(resultado);
    }

    private record Linha(Produto produto, int quantidade, BigDecimal preco, BigDecimal custo,
                         BigDecimal subtotal) {}
    private record PagamentoCalculado(FormaPagamento forma, BigDecimal valor) {}
}
