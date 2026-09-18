package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.entities.Produto;
import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.repositories.ProdutoRepository;
import com.lojaagro.estoque_api.repositories.CategoriaRepository;
import com.lojaagro.estoque_api.dto.ProdutoRequest;
import com.lojaagro.estoque_api.entities.Categoria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;
    private final TransacaoService transacaoService;
    private final FluxoCaixaService fluxoCaixaService;
    private final CategoriaRepository categoriaRepository;

    // CONSTRUTOR ATUALIZADO
    public ProdutoService(ProdutoRepository repository, 
                          TransacaoService transacaoService, 
                          FluxoCaixaService fluxoCaixaService,
                          CategoriaRepository categoriaRepository) {
        this.repository = repository;
        this.transacaoService = transacaoService;
        this.fluxoCaixaService = fluxoCaixaService;
        this.categoriaRepository = categoriaRepository;
    }

    // === MÉTODOS EXISTENTES (JÁ TINHA) ===
    public List<Produto> buscarTodos() {
        return repository.findAll();
    }

    public Optional<Produto> buscarPorId(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Produto criar(ProdutoRequest request) {
        Categoria categoria = obterOuCriarCategoria(request.categoria().nome());
        Produto produto = new Produto();
        produto.atualizarDados(
                request.nome(), request.tipo(), request.preco(),
                request.dataValidade(), categoria);
        produto.inicializarEstoque(request.quantidadeEstoque(), request.custoUnitario());
        return repository.save(produto);
    }

    @Transactional
    public Produto atualizar(Long id, ProdutoRequest request) {
        Produto produto = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        Categoria categoria = obterOuCriarCategoria(request.categoria().nome());
        produto.atualizarDados(
                request.nome(), request.tipo(), request.preco(),
                request.dataValidade(), categoria);
        return repository.save(produto);
    }

    private Categoria obterOuCriarCategoria(String nome) {
        String nomeNormalizado = nome.trim();
        return categoriaRepository.findByNomeIgnoreCase(nomeNormalizado)
                .orElseGet(() -> categoriaRepository.save(new Categoria(nomeNormalizado)));
    }

    public void deletar(Long id) {
        Produto produto = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
            
        // Forçamos o false na mão e salvamos! 
        // Assim, até os produtos velhos obedecem à lixeira.
        produto.setAtivo(false);
        repository.save(produto);
    }

    // === NOVOS MÉTODOS (ADICIONA ESSES) ===
    
    @Transactional
    public Produto venderComLucro(Long produtoId, int quantidade, BigDecimal precoVenda, Usuario usuario) {
        Produto produto = repository.findById(produtoId)
            .orElseThrow(() -> new IllegalArgumentException("ERRO FATAL: Produto não encontrado."));
        
        if (produto.getQuantidadeEstoque() < quantidade) {
            throw new IllegalArgumentException("Estoque insuficiente! Disponível: " + produto.getQuantidadeEstoque());
        }
        
        BigDecimal custoUnitario = produto.getCustoMedio();
        BigDecimal lucroTotal = precoVenda.subtract(custoUnitario)
                .multiply(BigDecimal.valueOf(quantidade));

        produto.venderProduto(quantidade);
        repository.save(produto);
        
        BigDecimal valorTotal = precoVenda.multiply(BigDecimal.valueOf(quantidade));
        
        transacaoService.registrarTransacao(
            produto, 
            usuario, 
            "VENDA", 
            quantidade, 
            precoVenda,
            custoUnitario,
            lucroTotal,
            "Venda de " + quantidade + "x " + produto.getNome()
        );
        
        fluxoCaixaService.adicionarEntrada(valorTotal);
        
        return produto;
    }

    @Transactional
    public Produto comprarComCusto(Long produtoId, int quantidade, BigDecimal precoCompra, Usuario usuario) {
        Produto produto = repository.findById(produtoId)
            .orElseThrow(() -> new IllegalArgumentException("ERRO FATAL: Produto não encontrado."));
        
        produto.comprarProduto(quantidade, precoCompra);
        repository.save(produto);
        
        BigDecimal valorTotal = precoCompra.multiply(BigDecimal.valueOf(quantidade));
        
        transacaoService.registrarTransacao(
            produto,
            usuario,
            "COMPRA",
            quantidade,
            precoCompra,
            precoCompra,
            BigDecimal.ZERO,
            "Reposição de " + quantidade + "x " + produto.getNome()
        );
        
        fluxoCaixaService.adicionarSaida(valorTotal);
        
        return produto;
    }
}
