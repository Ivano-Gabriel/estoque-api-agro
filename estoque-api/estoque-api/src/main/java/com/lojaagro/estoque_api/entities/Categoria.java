package com.lojaagro.estoque_api.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity




public class Categoria {

    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)


    
    private Long id;
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loja_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Loja loja;
    
    public Categoria() {}
    
   
    public Categoria(String nome) {

       
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("ERRO FATAL: O nome da categoria não pode ser vazio.");
        }
        
        this.nome = nome;
        
    }

    public Categoria(String nome, Loja loja) {
        this(nome);
        this.loja = loja;
    }

    // Getters
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public Loja getLoja() { return loja; }
    public void setLoja(Loja loja) { this.loja = loja; }

    // toStrind q substitui o "mostrarDados()"
    @Override
    public String toString() {
        return "Categoria [ID: " + this.id + " | Nome: " + this.nome + "]";
    }
}
