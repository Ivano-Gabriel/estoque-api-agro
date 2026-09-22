package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.entities.Categoria;
import com.lojaagro.estoque_api.entities.Loja;
import com.lojaagro.estoque_api.repositories.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {
    private final CategoriaRepository categorias;
    private final FluxoCaixaService caixa;
    public CategoriaService(CategoriaRepository categorias, FluxoCaixaService caixa) {
        this.categorias = categorias; this.caixa = caixa;
    }
    public List<Categoria> buscarTodos(Loja loja) { return categorias.findByLojaIdOrderByNome(loja.getId()); }
    public Optional<Categoria> buscarPorId(Long id, Loja loja) { return categorias.findByIdAndLojaId(id, loja.getId()); }
    @Transactional
    public Categoria salvar(String nome, Loja loja) {
        caixa.bloquearOperacoes(loja.getId());
        String normalizado = nome.trim();
        return categorias.findByLojaIdAndNomeIgnoreCase(loja.getId(), normalizado)
                .orElseGet(() -> categorias.save(new Categoria(normalizado, loja)));
    }
}
