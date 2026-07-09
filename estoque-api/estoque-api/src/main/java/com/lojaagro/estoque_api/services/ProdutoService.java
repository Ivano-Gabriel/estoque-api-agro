package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.entities.Produto;
import com.lojaagro.estoque_api.repositories.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }


    public List<Produto> buscarTodos() {
        return repository.findAll();
    }


    public Optional<Produto> buscarPorId(Long id) {
        return repository.findById(id);
    }


    public Produto salvar(Produto produto) {
        return repository.save(produto);
    }

public Produto realizarVenda(Long id, int quantidadeComprada) {
        
        Produto produtoTemporario = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ERRO FATAL: Produto não encontrado."));

        produtoTemporario.venderProduto(quantidadeComprada);

        
        return repository.save(produtoTemporario);
    }


}