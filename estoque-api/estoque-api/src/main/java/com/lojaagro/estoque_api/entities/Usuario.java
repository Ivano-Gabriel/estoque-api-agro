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

    public Usuario() {}
    public void setSenha(String senha) {
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
}
