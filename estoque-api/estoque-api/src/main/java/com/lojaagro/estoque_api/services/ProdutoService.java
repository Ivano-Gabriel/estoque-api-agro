package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.entities.Produto;
import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.repositories.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;
    private final TransacaoService transacaoService;
    private final FluxoCaixaService fluxoCaixaService;

    // CONSTRUTOR ATUALIZADO
    public ProdutoService(ProdutoRepository repository, 
                          TransacaoService transacaoService, 
                          FluxoCaixaService fluxoCaixaService) {
        this.repository = repository;
        this.transacaoService = transacaoService;
        this.fluxoCaixaService = fluxoCaixaService;
    }

    // === MÉTODOS EXISTENTES (JÁ TINHA) ===
    public List<Produto> buscarTodos() {
        return repository.findAll();
    }

    public Optional<Produto> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Produto salvar(Produto produto) {
        return repository.save(produto);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }

    public Produto realizarVenda(Long id, int quantidadeComprada) {
        Produto produtoTemporario = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ERRO FATAL: Produto não encontrado."));
        produtoTemporario.venderProduto(quantidadeComprada);
        return repository.save(produtoTemporario);
    }

    public Produto realizarCompra(Long id, int quantidadeAbastecida) {
        Produto produtoTemporario = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ERRO FATAL: Produto não encontrado."));
        produtoTemporario.comprarProduto(quantidadeAbastecida);
        return repository.save(produtoTemporario);
    }

    // === NOVOS MÉTODOS (ADICIONA ESSES) ===
    
    @Transactional
    public Produto venderComLucro(Long produtoId, int quantidade, double precoVenda, Usuario usuario) {
        Produto produto = repository.findById(produtoId)
            .orElseThrow(() -> new IllegalArgumentException("ERRO FATAL: Produto não encontrado."));
        
        if (produto.getQuantidadeEstoque() < quantidade) {
            throw new IllegalArgumentException("Estoque insuficiente! Disponível: " + produto.getQuantidadeEstoque());
        }
        
        produto.venderProduto(quantidade);
        repository.save(produto);
        
        double valorTotal = quantidade * precoVenda;
        
        transacaoService.registrarTransacao(
            produto, 
            usuario, 
            "VENDA", 
            quantidade, 
            precoVenda, 
            "Venda de " + quantidade + "x " + produto.getNome()
        );
        
        fluxoCaixaService.adicionarEntrada(valorTotal);
        
        return produto;
    }

    @Transactional
    public Produto comprarComCusto(Long produtoId, int quantidade, double precoCompra, Usuario usuario) {
        Produto produto = repository.findById(produtoId)
            .orElseThrow(() -> new IllegalArgumentException("ERRO FATAL: Produto não encontrado."));
        
        produto.comprarProduto(quantidade);
        repository.save(produto);
        
        double valorTotal = quantidade * precoCompra;
        
        transacaoService.registrarTransacao(
            produto,
            usuario,
            "COMPRA",
            quantidade,
            precoCompra,
            "Reposição de " + quantidade + "x " + produto.getNome()
        );
        
        fluxoCaixaService.adicionarSaida(valorTotal);
        
        return produto;
    }
}