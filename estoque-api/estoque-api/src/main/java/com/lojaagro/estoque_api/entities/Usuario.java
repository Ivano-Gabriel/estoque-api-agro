package com.lojaagro.estoque_api.entities;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) default 'FUNCIONARIA'")
    private UsuarioRole role = UsuarioRole.FUNCIONARIA;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean ativo = true;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int versaoSessao = 0;

    @Version
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long versao;

    public Usuario() {}
    public void setSenha(String senha) {
        if (this.senha != null && !this.senha.equals(senha)) revogarSessoes();
        this.senha = senha;
    }
    
    public Usuario(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
    public UsuarioRole getRole() { return role; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(UsuarioRole role) { this.role = role; }
    public boolean isAtivo() { return ativo; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public int getVersaoSessao() { return versaoSessao; }
    public void revogarSessoes() { versaoSessao++; }
    public void setAtivo(boolean ativo) {
        if (this.ativo != ativo) revogarSessoes();
        this.ativo = ativo;
    }
}
