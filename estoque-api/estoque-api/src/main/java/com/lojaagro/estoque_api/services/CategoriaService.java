package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.entities.Categoria;
import com.lojaagro.estoque_api.repositories.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }


    public List<Categoria> buscarTodos() {
        return repository.findAll();
    }


    public Optional<Categoria> buscarPorId(Long id) {
        return repository.findById(id);
    }


    public Categoria salvar(Categoria categoria) {
        return repository.save(categoria);
    }




}