package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.entities.Categoria;
import com.lojaagro.estoque_api.repositories.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;
    private final FluxoCaixaService caixa;

    public CategoriaService(CategoriaRepository repository, FluxoCaixaService caixa) {
        this.repository = repository;
        this.caixa = caixa;
    }


    public List<Categoria> buscarTodos() {
        return repository.findAll();
    }


    public Optional<Categoria> buscarPorId(Long id) {
        return repository.findById(id);
    }


    @org.springframework.transaction.annotation.Transactional
    public Categoria salvar(Categoria categoria) {
        caixa.bloquearOperacoes();
        return repository.findByNomeIgnoreCase(categoria.getNome().trim())
                .orElseGet(() -> repository.save(new Categoria(categoria.getNome().trim())));
    }




}
