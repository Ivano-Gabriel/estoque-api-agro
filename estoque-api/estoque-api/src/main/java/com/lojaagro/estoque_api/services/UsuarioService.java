package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public Usuario buscarPorId(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }

    public Usuario buscarPorEmail(String email) {
        return repository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }
}
