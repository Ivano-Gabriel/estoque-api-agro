package com.lojaagro.estoque_api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "cliente", indexes = {
        @Index(name = "idx_cliente_loja_nome", columnList = "loja_id,nome")
})
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loja_id", nullable = false)
    @JsonIgnore
    private Loja loja;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 15)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(length = 500)
    private String observacoes;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @ManyToMany
    @JoinTable(name = "cliente_produto_favorito",
            joinColumns = @JoinColumn(name = "cliente_id"),
            inverseJoinColumns = @JoinColumn(name = "produto_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_cliente_produto_favorito", columnNames = {"cliente_id", "produto_id"}))
    private Set<Produto> produtosFavoritos = new LinkedHashSet<>();

    protected Cliente() {}

    public Cliente(Loja loja, String nome, String telefone, String email,
                   String observacoes, LocalDateTime criadoEm) {
        this.loja = loja;
        this.criadoEm = criadoEm;
        atualizar(nome, telefone, email, observacoes);
    }

    public void atualizar(String nome, String telefone, String email, String observacoes) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome do cliente é obrigatório.");
        this.nome = nome.trim();
        this.telefone = normalizarTelefone(telefone);
        this.email = email == null || email.isBlank() ? null : email.trim().toLowerCase(java.util.Locale.ROOT);
        this.observacoes = observacoes == null || observacoes.isBlank() ? null : observacoes.trim();
    }

    private String normalizarTelefone(String valor) {
        if (valor == null || valor.isBlank()) return null;
        String numero = valor.replaceAll("\\D", "");
        if (numero.length() < 10 || numero.length() > 15) {
            throw new IllegalArgumentException("Telefone deve conter DDD e número; DDI é opcional.");
        }
        return numero;
    }

    public Long getId() { return id; }
    public Loja getLoja() { return loja; }
    public String getNome() { return nome; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }
    public String getObservacoes() { return observacoes; }
    public boolean isAtivo() { return ativo; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public Set<Produto> getProdutosFavoritos() { return produtosFavoritos; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public void definirFavoritos(Set<Produto> favoritos) {
        produtosFavoritos.clear();
        produtosFavoritos.addAll(favoritos);
    }
}
