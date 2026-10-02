package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.dto.LanchoneteDtos;
import com.lojaagro.estoque_api.dto.LanchoneteDtos.Configuracao;
import com.lojaagro.estoque_api.dto.LanchoneteDtos.Grupo;
import com.lojaagro.estoque_api.dto.LanchoneteDtos.GrupoRequest;
import com.lojaagro.estoque_api.dto.LanchoneteDtos.ItemCardapioRequest;
import com.lojaagro.estoque_api.dto.LanchoneteDtos.ItemCardapioResponse;
import com.lojaagro.estoque_api.dto.LanchoneteDtos.Mesa;
import com.lojaagro.estoque_api.dto.LanchoneteDtos.MesaRequest;
import com.lojaagro.estoque_api.dto.LanchoneteDtos.PagamentoRequest;
import com.lojaagro.estoque_api.dto.LanchoneteDtos.PedidoRequest;
import com.lojaagro.estoque_api.dto.LanchoneteDtos.PedidoResponse;
import com.lojaagro.estoque_api.dto.VendaRequest;
import com.lojaagro.estoque_api.dto.VendaResponse;
import com.lojaagro.estoque_api.entities.Cliente;
import com.lojaagro.estoque_api.entities.FichaTecnicaItem;
import com.lojaagro.estoque_api.entities.FormaPagamento;
import com.lojaagro.estoque_api.entities.GrupoAdicional;
import com.lojaagro.estoque_api.entities.ItemCardapio;
import com.lojaagro.estoque_api.entities.Loja;
import com.lojaagro.estoque_api.entities.MesaLanchonete;
import com.lojaagro.estoque_api.entities.OpcaoAdicional;
import com.lojaagro.estoque_api.entities.PedidoLanchonete;
import com.lojaagro.estoque_api.entities.PedidoLanchoneteAdicional;
import com.lojaagro.estoque_api.entities.PedidoLanchoneteConsumo;
import com.lojaagro.estoque_api.entities.PedidoLanchoneteItem;
import com.lojaagro.estoque_api.entities.Produto;
import com.lojaagro.estoque_api.entities.StatusPedido;
import com.lojaagro.estoque_api.entities.TipoAtendimento;
import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.entities.UsuarioRole;
import com.lojaagro.estoque_api.repositories.FichaTecnicaItemRepository;
import com.lojaagro.estoque_api.repositories.GrupoAdicionalRepository;
import com.lojaagro.estoque_api.repositories.ItemCardapioRepository;
import com.lojaagro.estoque_api.repositories.MesaLanchoneteRepository;
import com.lojaagro.estoque_api.repositories.OpcaoAdicionalRepository;
import com.lojaagro.estoque_api.repositories.PedidoLanchoneteRepository;
import com.lojaagro.estoque_api.repositories.ProdutoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Service
public class LanchoneteService {
    private final ItemCardapioRepository itens;
    private final FichaTecnicaItemRepository fichas;
    private final GrupoAdicionalRepository grupos;
    private final OpcaoAdicionalRepository opcoes;
    private final MesaLanchoneteRepository mesas;
    private final PedidoLanchoneteRepository pedidos;
    private final ProdutoRepository produtos;
    private final ClienteService clientes;
    private final VendaService vendas;
    private final FluxoCaixaService trava;
    private final TransacaoService transacoes;
    private final AuditoriaService auditoria;
    private final Clock clock;

    public LanchoneteService(ItemCardapioRepository itens,
                             FichaTecnicaItemRepository fichas,
                             GrupoAdicionalRepository grupos,
                             OpcaoAdicionalRepository opcoes,
                             MesaLanchoneteRepository mesas,
                             PedidoLanchoneteRepository pedidos,
                             ProdutoRepository produtos,
                             ClienteService clientes,
                             VendaService vendas,
                             FluxoCaixaService trava,
                             TransacaoService transacoes,
                             AuditoriaService auditoria,
                             Clock clock) {
        this.itens = itens;
        this.fichas = fichas;
        this.grupos = grupos;
        this.opcoes = opcoes;
        this.mesas = mesas;
        this.pedidos = pedidos;
        this.produtos = produtos;
        this.clientes = clientes;
        this.vendas = vendas;
        this.trava = trava;
        this.transacoes = transacoes;
        this.auditoria = auditoria;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Configuracao configuracao(Loja loja) {
        exigirModulo(loja);
        List<PedidoLanchonete> abertos = pedidos.findTop100ByLojaIdAndStatusInOrderByCriadoEmAsc(
                loja.getId(), statusAbertos());
        Set<Long> ocupadas = new HashSet<>();
        abertos.forEach(pedido -> {
            if (pedido.getMesa() != null) ocupadas.add(pedido.getMesa().getId());
        });
        return new Configuracao(
                itens.findByLojaIdOrderByOrdemAscIdAsc(loja.getId()).stream()
                        .map(LanchoneteDtos::item).toList(),
                grupos.findByLojaIdOrderByOrdemAscIdAsc(loja.getId()).stream()
                        .map(LanchoneteDtos::grupo).toList(),
                mesas.findByLojaIdOrderByOrdemAscIdAsc(loja.getId()).stream()
                        .map(mesa -> new Mesa(mesa.getId(), mesa.getNome(), mesa.getLugares(),
                                mesa.isAtiva(), mesa.getOrdem(), ocupadas.contains(mesa.getId())))
                        .toList());
    }

    @Transactional(readOnly = true)
    public List<ItemCardapioResponse> cardapio(Loja loja) {
        exigirModulo(loja);
        return itens.findByLojaIdOrderByOrdemAscIdAsc(loja.getId()).stream()
                .filter(ItemCardapio::isDisponivel)
                .filter(item -> item.getProduto().isAtivo())
                .map(LanchoneteDtos::item)
                .toList();
    }

    @Transactional
    public ItemCardapioResponse salvarItem(Long id, ItemCardapioRequest request, Usuario usuario) {
        exigirAdmin(usuario);
        Loja loja = usuario.getLoja();
        Produto produto = produto(request.produtoId(), loja);
        ItemCardapio item = id == null
                ? new ItemCardapio(loja, produto)
                : itens.findByIdAndLojaId(id, loja.getId())
                .orElseThrow(() -> new IllegalArgumentException("Item do cardápio não encontrado."));
        if (id == null && itens.findByLojaIdOrderByOrdemAscIdAsc(loja.getId()).stream()
                .anyMatch(outro -> outro.getProduto().getId().equals(produto.getId()))) {
            throw new IllegalArgumentException("Produto já está no cardápio.");
        }

        boolean baixaProdutoFinal = request.baixaProdutoFinal()
                && listaSegura(request.ingredientes()).isEmpty();
        configurarControleDoProduto(produto, baixaProdutoFinal);
        item.configurar(request.nomeCozinha(), request.estacao(), request.tempoPreparoMinutos(),
                request.disponivel(), request.destaque(), baixaProdutoFinal, request.ordem());

        List<FichaTecnicaItem> ficha = new ArrayList<>();
        Set<Long> ids = new HashSet<>();
        for (var ingredienteRequest : listaSegura(request.ingredientes())) {
            if (!ids.add(ingredienteRequest.produtoId())) {
                throw new IllegalArgumentException("Ingrediente repetido na ficha técnica.");
            }
            Produto ingrediente = insumo(ingredienteRequest.produtoId(), loja);
            if (ingrediente.getId().equals(produto.getId())) {
                throw new IllegalArgumentException("O item de venda não pode ser ingrediente dele mesmo.");
            }
            ficha.add(new FichaTecnicaItem(item, ingrediente, ingredienteRequest.quantidade()));
        }
        item.substituirIngredientes(ficha);

        List<GrupoAdicional> gruposDoItem = new ArrayList<>();
        for (Long grupoId : conjuntoSeguro(request.grupoIds())) {
            gruposDoItem.add(grupos.findByIdAndLojaId(grupoId, loja.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Grupo de adicionais não encontrado.")));
        }
        item.substituirGrupos(gruposDoItem);
        ItemCardapio salvo = itens.save(item);
        auditoria.registrar(usuario, id == null ? "CRIAR" : "ATUALIZAR",
                "ITEM_CARDAPIO", salvo.getId(), produto.getNome());
        return LanchoneteDtos.item(salvo);
    }

    @Transactional
    public Grupo salvarGrupo(Long id, GrupoRequest request, Usuario usuario) {
        exigirAdmin(usuario);
        Loja loja = usuario.getLoja();
        GrupoAdicional grupo = id == null
                ? new GrupoAdicional(loja)
                : grupos.findByIdAndLojaId(id, loja.getId())
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado."));
        grupo.configurar(request.nome(), request.minimo(), request.maximo(),
                request.obrigatorio(), request.ativo(), request.ordem());

        Map<Long, OpcaoAdicional> existentes = new HashMap<>();
        grupo.getOpcoes().forEach(opcao -> existentes.put(opcao.getProduto().getId(), opcao));
        List<OpcaoAdicional> novas = new ArrayList<>();
        Set<Long> vistos = new HashSet<>();
        for (var opcaoRequest : request.opcoes()) {
            if (!vistos.add(opcaoRequest.produtoId())) {
                throw new IllegalArgumentException("Opção repetida no grupo.");
            }
            Produto venda = produto(opcaoRequest.produtoId(), loja);
            tornarProdutoDeVenda(venda);
            Produto ingrediente = opcaoRequest.ingredienteId() == null
                    ? null : insumo(opcaoRequest.ingredienteId(), loja);
            if (opcaoRequest.quantidadeInsumo() > 0 && ingrediente == null) {
                throw new IllegalArgumentException("Selecione o insumo consumido pela opção.");
            }
            if (ingrediente != null && ingrediente.getId().equals(venda.getId())) {
                throw new IllegalArgumentException("A opção de venda não pode ser o próprio insumo.");
            }
            OpcaoAdicional opcao = existentes.get(opcaoRequest.produtoId());
            if (opcao == null) {
                opcao = new OpcaoAdicional(grupo, venda, ingrediente,
                        opcaoRequest.quantidadeInsumo(), opcaoRequest.ativo(), opcaoRequest.ordem());
            } else {
                opcao.configurar(ingrediente, opcaoRequest.quantidadeInsumo(),
                        opcaoRequest.ativo(), opcaoRequest.ordem());
            }
            novas.add(opcao);
        }
        grupo.sincronizarOpcoes(novas);
        GrupoAdicional salvo = grupos.save(grupo);
        auditoria.registrar(usuario, id == null ? "CRIAR" : "ATUALIZAR",
                "GRUPO_ADICIONAL", salvo.getId(), salvo.getNome());
        return LanchoneteDtos.grupo(salvo);
    }

    @Transactional
    public Mesa salvarMesa(Long id, MesaRequest request, Usuario usuario) {
        exigirAdmin(usuario);
        MesaLanchonete mesa = id == null
                ? new MesaLanchonete(usuario.getLoja())
                : mesas.findByIdAndLojaId(id, usuario.getLoja().getId())
                .orElseThrow(() -> new IllegalArgumentException("Mesa não encontrada."));
        mesa.configurar(request.nome(), request.lugares(), request.ativa(), request.ordem());
        mesa = mesas.save(mesa);
        auditoria.registrar(usuario, id == null ? "CRIAR" : "ATUALIZAR",
                "MESA", mesa.getId(), mesa.getNome());
        return new Mesa(mesa.getId(), mesa.getNome(), mesa.getLugares(),
                mesa.isAtiva(), mesa.getOrdem(), false);
    }

    @Transactional
    public PedidoResponse criar(UUID id, PedidoRequest request, Usuario usuario) {
        exigirOperador(usuario);
        Loja loja = usuario.getLoja();
        String assinatura = assinar(request);
        Optional<PedidoLanchonete> existente = pedidos.findByIdAndLojaId(id, loja.getId());
        if (existente.isPresent()) return validarRepeticao(existente.get(), assinatura);

        trava.bloquearOperacoes(loja.getId());
        existente = pedidos.findByIdAndLojaId(id, loja.getId());
        if (existente.isPresent()) return validarRepeticao(existente.get(), assinatura);

        MesaLanchonete mesa = validarAtendimento(request, loja);
        Cliente cliente = clientes.entidadeOpcional(request.clienteId(), loja);
        List<Linha> linhas = new ArrayList<>();
        Map<Long, Integer> consumos = new LinkedHashMap<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (var solicitado : request.itens()) {
            ItemCardapio item = itens.findByIdAndLojaId(solicitado.itemCardapioId(), loja.getId())
                    .filter(ItemCardapio::isDisponivel)
                    .filter(encontrado -> encontrado.getProduto().isAtivo())
                    .orElseThrow(() -> new IllegalArgumentException("Item indisponível no cardápio."));
            Set<Long> selecionadas = solicitado.opcaoIds() == null ? Set.of() : solicitado.opcaoIds();
            validarGrupos(item, selecionadas, loja.getId());
            List<OpcaoAdicional> adicionais = new ArrayList<>();
            for (Long opcaoId : selecionadas) {
                OpcaoAdicional opcao = opcoes.findByIdAndGrupoLojaId(opcaoId, loja.getId())
                        .filter(OpcaoAdicional::isAtivo)
                        .filter(valor -> valor.getGrupo().isAtivo())
                        .filter(valor -> valor.getProduto().isAtivo())
                        .orElseThrow(() -> new IllegalArgumentException("Adicional indisponível."));
                if (!item.getGrupos().contains(opcao.getGrupo())) {
                    throw new IllegalArgumentException("Adicional não pertence ao item selecionado.");
                }
                adicionais.add(opcao);
            }

            BigDecimal valorLinha = item.getProduto().getPreco();
            for (OpcaoAdicional adicional : adicionais) {
                valorLinha = valorLinha.add(adicional.getProduto().getPreco());
            }
            subtotal = subtotal.add(valorLinha.multiply(BigDecimal.valueOf(solicitado.quantidade())));
            for (FichaTecnicaItem ingrediente : item.getIngredientes()) {
                somar(consumos, ingrediente.getIngrediente().getId(),
                        multiplicar(ingrediente.getQuantidade(), solicitado.quantidade()));
            }
            for (OpcaoAdicional adicional : adicionais) {
                if (adicional.getIngrediente() != null && adicional.getQuantidadeInsumo() > 0) {
                    somar(consumos, adicional.getIngrediente().getId(),
                            multiplicar(adicional.getQuantidadeInsumo(), solicitado.quantidade()));
                }
            }
            linhas.add(new Linha(item, solicitado.quantidade(), solicitado.observacoes(), adicionais));
        }

        long numero = pedidos.maiorNumero(loja.getId()) + 1;
        PedidoLanchonete pedido = new PedidoLanchonete(id, loja, usuario, cliente, mesa,
                assinatura, numero, request.tipo(), request.identificacao(), request.telefone(),
                request.endereco(), request.observacoes(), subtotal, request.desconto(),
                LocalDateTime.now(clock));
        for (Linha linha : linhas) {
            PedidoLanchoneteItem itemPedido = new PedidoLanchoneteItem(pedido, linha.item(),
                    linha.quantidade(), linha.item().getProduto().getPreco(), linha.observacoes());
            linha.adicionais().forEach(adicional ->
                    itemPedido.adicionar(new PedidoLanchoneteAdicional(itemPedido, adicional)));
            pedido.adicionarItem(itemPedido);
        }

        for (Map.Entry<Long, Integer> consumo : consumos.entrySet()) {
            Produto insumo = produtos.bloquearPorIdELoja(consumo.getKey(), loja.getId())
                    .filter(Produto::isAtivo)
                    .filter(Produto::isControlaEstoque)
                    .orElseThrow(() -> new IllegalArgumentException("Insumo indisponível."));
            insumo.venderProduto(consumo.getValue());
            pedido.adicionarConsumo(new PedidoLanchoneteConsumo(
                    pedido, insumo, consumo.getValue(), insumo.getCustoMedio()));
            transacoes.registrarTransacao(insumo, usuario, "CONSUMO_LANCHONETE", consumo.getValue(),
                    BigDecimal.ZERO, BigDecimal.ZERO, insumo.getCustoMedio(), BigDecimal.ZERO,
                    "Pedido #" + numero, null, null, FormaPagamento.NAO_INFORMADO);
        }
        pedidos.saveAndFlush(pedido);
        auditoria.registrar(usuario, "CRIAR", "PEDIDO_LANCHONETE", id,
                "Pedido #" + numero + " • " + request.tipo());
        return LanchoneteDtos.pedido(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> fila(Loja loja, boolean historico) {
        exigirModulo(loja);
        List<PedidoLanchonete> resultado = historico
                ? pedidos.findTop100ByLojaIdOrderByCriadoEmDesc(loja.getId())
                : pedidos.findTop100ByLojaIdAndStatusInOrderByCriadoEmAsc(
                loja.getId(), statusAbertos());
        return resultado.stream().map(LanchoneteDtos::pedido).toList();
    }

    @Transactional
    public PedidoResponse status(UUID id, StatusPedido novo, Usuario usuario) {
        PedidoLanchonete pedido = bloquear(id, usuario);
        pedido.avancar(novo, LocalDateTime.now(clock));
        auditoria.registrar(usuario, "STATUS", "PEDIDO_LANCHONETE", id, novo.name());
        return LanchoneteDtos.pedido(pedido);
    }

    @Transactional
    public PedidoResponse pagar(UUID id, PagamentoRequest request, Usuario usuario) {
        PedidoLanchonete pedido = bloquear(id, usuario);
        if (pedido.getStatus() == StatusPedido.FINALIZADO) return LanchoneteDtos.pedido(pedido);
        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new IllegalArgumentException("Pedido cancelado.");
        }

        Map<Long, Integer> quantidades = new LinkedHashMap<>();
        Map<Long, BigDecimal> precos = new HashMap<>();
        BigDecimal valorAtual = BigDecimal.ZERO;
        for (PedidoLanchoneteItem item : pedido.getItens()) {
            somar(quantidades, item.getProduto().getId(), item.getQuantidade());
            precos.put(item.getProduto().getId(), item.getProduto().getPreco());
            valorAtual = valorAtual.add(item.getProduto().getPreco()
                    .multiply(BigDecimal.valueOf(item.getQuantidade())));
            for (PedidoLanchoneteAdicional adicional : item.getAdicionais()) {
                int quantidade = multiplicar(adicional.getQuantidade(), item.getQuantidade());
                somar(quantidades, adicional.getProduto().getId(), quantidade);
                precos.put(adicional.getProduto().getId(), adicional.getProduto().getPreco());
                valorAtual = valorAtual.add(adicional.getProduto().getPreco()
                        .multiply(BigDecimal.valueOf(quantidade)));
            }
        }
        if (valorAtual.setScale(2, RoundingMode.HALF_UP).compareTo(pedido.getSubtotal()) != 0) {
            throw new IllegalArgumentException(
                    "O cardápio mudou após o envio. Revise os preços antes de receber.");
        }

        BigDecimal custoTotal = pedido.getConsumos().stream()
                .map(consumo -> consumo.getCustoUnitario()
                        .multiply(BigDecimal.valueOf(consumo.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        Map<Long, BigDecimal> custosUnitarios = ratearCusto(
                quantidades, precos, valorAtual, custoTotal);

        VendaRequest vendaRequest = new VendaRequest(
                pedido.getCliente() == null ? null : pedido.getCliente().getId(),
                request.formaPagamento(), pedido.getDesconto(), request.valorRecebido(),
                quantidades.entrySet().stream()
                        .map(entry -> new VendaRequest.Item(entry.getKey(), entry.getValue())).toList(),
                request.pagamentos());
        VendaResponse venda = vendas.concluirPedido(
                pedido.getId(), vendaRequest, usuario, custosUnitarios);
        pedido.finalizar(vendas.entidade(venda.id(), usuario.getLoja()), LocalDateTime.now(clock));
        auditoria.registrar(usuario, "FINALIZAR", "PEDIDO_LANCHONETE", id,
                "Venda " + venda.id() + " • custo de insumos " + custoTotal);
        return LanchoneteDtos.pedido(pedido);
    }

    @Transactional
    public PedidoResponse cancelar(UUID id, String motivo, Usuario usuario) {
        PedidoLanchonete pedido = bloquear(id, usuario);
        if (pedido.getStatus() == StatusPedido.CANCELADO) return LanchoneteDtos.pedido(pedido);
        if (pedido.getStatus() == StatusPedido.FINALIZADO) {
            if (usuario.getRole() != UsuarioRole.ADMIN) {
                throw new AccessDeniedException("Somente administradora cancela pedido já pago.");
            }
            vendas.cancelarPedido(pedido.getVenda().getId(), motivo, usuario);
        }
        for (PedidoLanchoneteConsumo consumo : pedido.getConsumos()) {
            if (!consumo.isRestaurado()) {
                consumo.getProduto().reporSemCusto(consumo.getQuantidade());
                consumo.restaurar();
                transacoes.registrarTransacao(consumo.getProduto(), usuario, "ESTORNO_CONSUMO",
                        consumo.getQuantidade(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, "Cancelamento do pedido #" + pedido.getNumero(),
                        null, null, FormaPagamento.NAO_INFORMADO);
            }
        }
        pedido.cancelar(motivo, LocalDateTime.now(clock));
        auditoria.registrar(usuario, "CANCELAR", "PEDIDO_LANCHONETE", id, motivo);
        return LanchoneteDtos.pedido(pedido);
    }

    private Map<Long, BigDecimal> ratearCusto(Map<Long, Integer> quantidades,
                                               Map<Long, BigDecimal> precos,
                                               BigDecimal receitaTotal,
                                               BigDecimal custoTotal) {
        Map<Long, BigDecimal> resultado = new HashMap<>();
        if (receitaTotal.signum() <= 0 || custoTotal.signum() <= 0) return resultado;
        for (Map.Entry<Long, Integer> entry : quantidades.entrySet()) {
            BigDecimal receitaProduto = precos.get(entry.getKey())
                    .multiply(BigDecimal.valueOf(entry.getValue()));
            BigDecimal custoProduto = custoTotal.multiply(receitaProduto)
                    .divide(receitaTotal, 8, RoundingMode.HALF_UP);
            resultado.put(entry.getKey(), custoProduto
                    .divide(BigDecimal.valueOf(entry.getValue()), 2, RoundingMode.HALF_UP));
        }
        return resultado;
    }

    private PedidoLanchonete bloquear(UUID id, Usuario usuario) {
        exigirOperador(usuario);
        return pedidos.bloquear(id, usuario.getLoja().getId())
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado nesta loja."));
    }

    private MesaLanchonete validarAtendimento(PedidoRequest request, Loja loja) {
        if (request.tipo() == TipoAtendimento.MESA) {
            if (request.mesaId() == null) throw new IllegalArgumentException("Selecione a mesa.");
            MesaLanchonete mesa = mesas.findByIdAndLojaId(request.mesaId(), loja.getId())
                    .filter(MesaLanchonete::isAtiva)
                    .orElseThrow(() -> new IllegalArgumentException("Mesa indisponível."));
            boolean ocupada = pedidos.findTop100ByLojaIdAndStatusInOrderByCriadoEmAsc(
                            loja.getId(), List.of(StatusPedido.RECEBIDO, StatusPedido.EM_PREPARO,
                                    StatusPedido.PRONTO))
                    .stream().anyMatch(pedido -> pedido.getMesa() != null
                            && pedido.getMesa().getId().equals(mesa.getId()));
            if (ocupada) throw new IllegalArgumentException("Mesa já possui pedido aberto.");
            return mesa;
        }
        if (request.tipo() == TipoAtendimento.ENTREGA
                && (request.endereco() == null || request.endereco().isBlank())) {
            throw new IllegalArgumentException("Endereço é obrigatório para entrega.");
        }
        if ((request.tipo() == TipoAtendimento.ENTREGA
                || request.tipo() == TipoAtendimento.RETIRADA)
                && (request.identificacao() == null || request.identificacao().isBlank())) {
            throw new IllegalArgumentException("Identificação do cliente é obrigatória.");
        }
        return null;
    }

    private void validarGrupos(ItemCardapio item, Set<Long> selecionadas, Long lojaId) {
        Map<Long, Long> contagem = new HashMap<>();
        for (Long opcaoId : selecionadas) {
            OpcaoAdicional opcao = opcoes.findByIdAndGrupoLojaId(opcaoId, lojaId)
                    .filter(OpcaoAdicional::isAtivo)
                    .filter(valor -> valor.getGrupo().isAtivo())
                    .orElseThrow(() -> new IllegalArgumentException("Adicional indisponível."));
            contagem.merge(opcao.getGrupo().getId(), 1L, Long::sum);
        }
        for (GrupoAdicional grupo : item.getGrupos()) {
            if (!grupo.isAtivo()) continue;
            long quantidade = contagem.getOrDefault(grupo.getId(), 0L);
            if (quantidade < grupo.getMinimo() || quantidade > grupo.getMaximo()) {
                throw new IllegalArgumentException("Escolha de " + grupo.getMinimo() + " a "
                        + grupo.getMaximo() + " opção(ões) em " + grupo.getNome() + ".");
            }
        }
        boolean estranho = contagem.keySet().stream().anyMatch(grupoId -> item.getGrupos().stream()
                .filter(GrupoAdicional::isAtivo)
                .noneMatch(grupo -> grupo.getId().equals(grupoId)));
        if (estranho) throw new IllegalArgumentException("Há adicional que não pertence ao item.");
    }

    private PedidoResponse validarRepeticao(PedidoLanchonete pedido, String assinatura) {
        if (!MessageDigest.isEqual(pedido.getAssinatura().getBytes(StandardCharsets.UTF_8),
                assinatura.getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("Esta chave já foi usada em outro pedido.");
        }
        return LanchoneteDtos.pedido(pedido);
    }

    private Produto produto(Long id, Loja loja) {
        return produtos.findByIdAndLojaIdAndAtivoTrue(id, loja.getId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado nesta loja."));
    }

    private Produto insumo(Long id, Loja loja) {
        Produto produto = produto(id, loja);
        if (!produto.isControlaEstoque()) {
            throw new IllegalArgumentException(produto.getNome()
                    + " é item de venda e não pode ser usado como insumo.");
        }
        return produto;
    }

    private void configurarControleDoProduto(Produto produto, boolean baixaProdutoFinal) {
        if (fichas.existsByIngredienteId(produto.getId())
                || opcoes.existsByIngredienteId(produto.getId())) {
            throw new IllegalArgumentException(produto.getNome()
                    + " já é usado como insumo e não pode virar item de venda.");
        }
        produto.setControlaEstoque(baixaProdutoFinal);
    }

    private void exigirModulo(Loja loja) {
        if (loja == null || !loja.isAtiva() || !loja.isLanchoneteAtiva()) {
            throw new AccessDeniedException("Módulo de lanchonete não está ativo para esta loja.");
        }
    }

    private void exigirOperador(Usuario usuario) {
        exigirModulo(usuario.getLoja());
    }

    private void exigirAdmin(Usuario usuario) {
        exigirModulo(usuario.getLoja());
        if (usuario.getRole() != UsuarioRole.ADMIN) {
            throw new AccessDeniedException("Somente administradora configura a lanchonete.");
        }
    }

    private String assinar(PedidoRequest request) {
        try {
            List<String> linhas = request.itens().stream()
                    .map(item -> item.itemCardapioId() + ":" + item.quantidade() + ":"
                            + Objects.toString(item.observacoes(), "") + ":"
                            + new TreeSet<>(item.opcaoIds() == null ? Set.of() : item.opcaoIds()))
                    .sorted().toList();
            String texto = request.tipo() + "|" + request.clienteId() + "|" + request.mesaId()
                    + "|" + Objects.toString(request.identificacao(), "")
                    + "|" + Objects.toString(request.telefone(), "")
                    + "|" + Objects.toString(request.endereco(), "")
                    + "|" + Objects.toString(request.observacoes(), "")
                    + "|" + request.desconto() + "|" + linhas;
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private List<StatusPedido> statusAbertos() {
        return List.of(StatusPedido.RECEBIDO, StatusPedido.EM_PREPARO,
                StatusPedido.PRONTO, StatusPedido.SAIU_PARA_ENTREGA);
    }

    private <T> List<T> listaSegura(List<T> lista) {
        return lista == null ? List.of() : lista;
    }

    private <T> Set<T> conjuntoSeguro(Set<T> conjunto) {
        return conjunto == null ? Set.of() : conjunto;
    }

    private void somar(Map<Long, Integer> mapa, Long id, int quantidade) {
        try {
            mapa.merge(id, quantidade, (atual, nova) -> Math.addExact(atual, nova));
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Quantidade total de insumo excede o limite.");
        }
    }

    private int multiplicar(int primeiro, int segundo) {
        try {
            return Math.multiplyExact(primeiro, segundo);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Quantidade de insumo excede o limite.");
        }
    }

    private record Linha(ItemCardapio item, int quantidade, String observacoes,
                         List<OpcaoAdicional> adicionais) {}
}
