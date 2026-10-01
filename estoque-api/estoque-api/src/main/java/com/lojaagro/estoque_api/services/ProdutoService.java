package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.dto.ProdutoRequest;
import com.lojaagro.estoque_api.entities.Categoria;
import com.lojaagro.estoque_api.entities.Loja;
import com.lojaagro.estoque_api.entities.Produto;
import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.entities.Cliente;
import com.lojaagro.estoque_api.entities.Transacao;
import com.lojaagro.estoque_api.repositories.CategoriaRepository;
import com.lojaagro.estoque_api.repositories.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;

@Service
public class ProdutoService {
    private final ProdutoRepository produtos;
    private final TransacaoService transacoes;
    private final FluxoCaixaService caixa;
    private final CategoriaRepository categorias;
    private final CloudinaryAssinaturaService cloudinary;
    private final ApplicationEventPublisher eventos;

    public ProdutoService(ProdutoRepository produtos, TransacaoService transacoes,
                          FluxoCaixaService caixa, CategoriaRepository categorias,
                          CloudinaryAssinaturaService cloudinary, ApplicationEventPublisher eventos) {
        this.produtos = produtos; this.transacoes = transacoes; this.caixa = caixa; this.categorias = categorias;
        this.cloudinary = cloudinary; this.eventos = eventos;
    }

    public List<Produto> buscarTodos(Loja loja) { return produtos.findByLojaIdAndAtivoTrue(loja.getId()); }
    public Optional<Produto> buscarPorId(Long id, Loja loja) {
        return produtos.findByIdAndLojaIdAndAtivoTrue(id, loja.getId());
    }

    @Transactional
    public Produto criar(ProdutoRequest request, Loja loja) {
        caixa.bloquearOperacoes(loja.getId());
        validarValoresFinanceiros(request, loja);
        validarDuplicado(loja, request, null);
        Categoria categoria = obterOuCriarCategoria(loja, request.categoria().nome());
        Produto produto = new Produto();
        produto.setLoja(loja);
        produto.atualizarDados(request.nome(), request.tipo(), request.preco(), request.dataValidade(),
                categoria, request.descricao(), validarImagem(request.imagemUrl(), loja),
                request.sku(), request.codigoBarras(), request.variacao(), request.estoqueMinimoSeguro());
        produto.inicializarEstoque(request.quantidadeEstoque(), request.custoUnitario());
        return produtos.save(produto);
    }

    @Transactional
    public Produto atualizar(Long id, ProdutoRequest request, Loja loja) {
        caixa.bloquearOperacoes(loja.getId());
        validarValoresFinanceiros(request, loja);
        validarDuplicado(loja, request, id);
        Produto produto = produtos.findByIdAndLojaIdAndAtivoTrue(id, loja.getId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        String imagemAnterior = produto.getImagemUrl();
        Categoria categoria = obterOuCriarCategoria(loja, request.categoria().nome());
        produto.atualizarDados(request.nome(), request.tipo(), request.preco(),
                request.dataValidade() == null ? produto.getDataValidade() : request.dataValidade(),
                categoria, request.descricao(), validarImagem(request.imagemUrl(), loja),
                request.sku(), request.codigoBarras(), request.variacao(), request.estoqueMinimoSeguro());
        Produto salvo = produtos.save(produto);
        if (imagemAnterior != null && !imagemAnterior.equals(salvo.getImagemUrl()))
            eventos.publishEvent(new CloudinaryAssinaturaService.ImagemRemovidaEvento(imagemAnterior));
        return salvo;
    }

    @Transactional
    public void deletar(Long id, Loja loja) {
        caixa.bloquearOperacoes(loja.getId());
        Produto produto = produtos.findByIdAndLojaIdAndAtivoTrue(id, loja.getId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        produto.setAtivo(false);
        produtos.save(produto);
    }

    @Transactional
    public Transacao venderComLucro(Long produtoId, int quantidade, BigDecimal precoInformado,
                                    Usuario usuario, Cliente cliente) {
        Loja loja = usuario.getLoja();
        Produto produto = produtos.findByIdAndLojaIdAndAtivoTrue(produtoId, loja.getId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        BigDecimal preco = loja.isFinanceiroAtivo() ? exigirPositivo(precoInformado, "Preço de venda") : BigDecimal.ZERO;
        BigDecimal custo = loja.isFinanceiroAtivo() ? produto.getCustoMedio() : BigDecimal.ZERO;
        BigDecimal lucro = loja.isFinanceiroAtivo()
                ? preco.subtract(custo).multiply(BigDecimal.valueOf(quantidade)) : BigDecimal.ZERO;
        produto.venderProduto(quantidade);
        produtos.save(produto);
        Transacao transacao = transacoes.registrarTransacao(produto, usuario, "VENDA", quantidade, preco, custo, lucro,
                "Saída de " + quantidade + "x " + produto.getNome(), cliente);
        if (loja.isFinanceiroAtivo()) caixa.adicionarEntrada(loja.getId(), preco.multiply(BigDecimal.valueOf(quantidade)));
        return transacao;
    }

    @Transactional
    public Transacao comprarComCusto(Long produtoId, int quantidade, BigDecimal precoInformado, Usuario usuario) {
        Loja loja = usuario.getLoja();
        Produto produto = produtos.findByIdAndLojaIdAndAtivoTrue(produtoId, loja.getId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        BigDecimal preco = loja.isFinanceiroAtivo() ? exigirPositivo(precoInformado, "Custo da reposição") : BigDecimal.ZERO;
        if (loja.isFinanceiroAtivo()) produto.comprarProduto(quantidade, preco);
        else produto.reporSemCusto(quantidade);
        produtos.save(produto);
        Transacao transacao = transacoes.registrarTransacao(produto, usuario, "COMPRA", quantidade, preco, preco, BigDecimal.ZERO,
                "Reposição de " + quantidade + "x " + produto.getNome(), null);
        if (loja.isFinanceiroAtivo()) caixa.adicionarSaida(loja.getId(), preco.multiply(BigDecimal.valueOf(quantidade)));
        return transacao;
    }

    @Transactional
    public void restaurar(Long id, Loja loja) {
        caixa.bloquearOperacoes(loja.getId());
        Produto produto = produtos.findByIdAndLojaId(id, loja.getId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        if (produtos.contarDuplicados(loja.getId(), produto.getNome(), produto.getCategoria().getNome(),
                produto.getVariacao(), id) > 0) {
            throw new IllegalArgumentException("Já existe um produto igual no catálogo.");
        }
        produto.setAtivo(true);
        produtos.save(produto);
    }

    public List<Produto> buscarLixeira(Loja loja) { return produtos.findByLojaIdAndAtivoFalse(loja.getId()); }

    private Categoria obterOuCriarCategoria(Loja loja, String nome) {
        String normalizado = nome.trim();
        return categorias.findByLojaIdAndNomeIgnoreCase(loja.getId(), normalizado)
                .orElseGet(() -> categorias.save(new Categoria(normalizado, loja)));
    }

    private void validarDuplicado(Loja loja, ProdutoRequest request, Long ignorarId) {
        if (produtos.contarDuplicados(loja.getId(), request.nome(), request.categoria().nome(),
                request.variacao(), ignorarId) > 0) {
            throw new IllegalArgumentException(
                    "Já existe um produto com esse nome, categoria e variação nesta loja, inclusive na lixeira.");
        }
        if (request.sku() != null && !request.sku().isBlank()
                && produtos.contarSku(loja.getId(), request.sku().trim(), ignorarId) > 0) {
            throw new IllegalArgumentException("SKU já utilizado por outro produto desta loja.");
        }
        if (request.codigoBarras() != null && !request.codigoBarras().isBlank()
                && produtos.contarCodigoBarras(loja.getId(), request.codigoBarras().trim(), ignorarId) > 0) {
            throw new IllegalArgumentException("Código de barras já utilizado por outro produto desta loja.");
        }
    }

    private void validarValoresFinanceiros(ProdutoRequest request, Loja loja) {
        if (!loja.isFinanceiroAtivo()) return;
        exigirPositivo(request.preco(), "Preço de venda");
        if (request.quantidadeEstoque() > 0) exigirPositivo(request.custoUnitario(), "Custo do estoque inicial");
    }

    private String validarImagem(String imagemUrl, Loja loja) {
        if (imagemUrl == null || imagemUrl.isBlank()) return null;
        if (!loja.isFotosAtivas()) {
            throw new IllegalArgumentException("O módulo de fotos não está ativo para esta loja.");
        }
        return cloudinary.validarUrl(imagemUrl, loja);
    }

    private BigDecimal exigirPositivo(BigDecimal valor, String campo) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(campo + " deve ser maior que zero.");
        }
        return valor;
    }
}
