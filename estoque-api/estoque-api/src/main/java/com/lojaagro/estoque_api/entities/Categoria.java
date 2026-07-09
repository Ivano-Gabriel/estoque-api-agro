package com.lojaagro.estoque_api.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity




public class Categoria {

    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)


    
    private Long id;
    private String nome;
    
    public Categoria() {}
    
    // Construtor defensivo
    public Categoria(String nome) {

        // peneirando espaço vazio 
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("ERRO FATAL: O nome da categoria não pode ser vazio.");
        }
        
        this.nome = nome;
        
    }

    // Getters
    public Long getId() { return id; }
    public String getNome() { return nome; }

    // toStrind q substitui o "mostrarDados()"
    @Override
    public String toString() {
        return "Categoria [ID: " + this.id + " | Nome: " + this.nome + "]";
    }
}