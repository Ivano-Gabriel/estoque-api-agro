package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import com.lojaagro.estoque_api.entities.Loja;

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

    public Usuario atual(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.security.access.AccessDeniedException("Sessão inválida.");
        }
        return buscarPorEmail(authentication.getName());
    }

    public Loja lojaAtual(Authentication authentication) {
        Usuario usuario = atual(authentication);
        if (usuario.getLoja() == null || !usuario.getLoja().isAtiva()) {
            throw new org.springframework.security.access.AccessDeniedException("Loja indisponível.");
        }
        return usuario.getLoja();
    }
}
