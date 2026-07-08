package com.lojaagro.estoque_api.entities;

public class Categoria {

    private int id;
    private String nome;
    private static int contadorGlobal = 0;

    // Construtor com Programação Defensiva
    public Categoria(String nome) {
        // Peneira de Nascimento: Impede categorias em branco ou nulas
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("ERRO FATAL: O nome da categoria não pode ser vazio.");
        }
        
        this.nome = nome;
        contadorGlobal++;
        this.id = contadorGlobal;
    }

    // Getters
    public int getId() { return id; }
    public String getNome() { return nome; }

    // O padrão de mercado que substitui o seu "mostrarDados()"
    @Override
    public String toString() {
        return "Categoria [ID: " + this.id + " | Nome: " + this.nome + "]";
    }
}