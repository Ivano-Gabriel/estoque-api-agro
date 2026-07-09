package com.lojaagro.estoque_api.entities;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Produto {

    
    private String nome;
    private String tipo;
    private double preco; 
    // No Spring Boot via ser BigDecimal
    private LocalDate dataValidade;

    @ManyToOne(cascade = jakarta.persistence.CascadeType.ALL)
    private Categoria categoria;
    
    public Produto() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantidadeEstoque = 0;

    // Construtor
    public Produto(String nome, String tipo, double preco, LocalDate dataValidade, Categoria categoria) {
        this.nome = nome;
        this.tipo = tipo;
        this.preco = preco;
        this.dataValidade = dataValidade;
        this.categoria = categoria;            
        
        
    }
        
    // Getters
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public double getPreco() { return preco; }
    public LocalDate getDataValidade() { return dataValidade; }
    public Categoria getCategoria() { return categoria; }
    public int getQuantidadeEstoque() { return quantidadeEstoque; }

    // Setters de Configuração Base
    public void setPreco(double novoPreco) {
        if (novoPreco <= 0) {
            System.out.println("ERRO: O preço deve ser maior que zero.");
            return; 
        }
        this.preco = novoPreco;
    }

    public void setQuantidadeEstoque(int novaQuantidade) {
        if (novaQuantidade < 0) {
            System.out.println("ERRO: Estoque não pode ser negativo.");
            return;
        }   
        this.quantidadeEstoque = novaQuantidade;
    }

    
    // MÉTODOS DE NEGÓCIO (A Inteligência)
    

    public void venderProduto(int quantidadeComprada) {
        if (quantidadeComprada <= 0) {
            System.out.println("ERRO: Quantidade de venda deve ser no mínimo 1.");
            return;
        }
        if (quantidadeComprada > this.quantidadeEstoque) {
            System.out.println("ERRO: Estoque insuficiente para " + this.nome + ". Venda bloqueada.");
            return;
        }
        
        this.quantidadeEstoque = this.quantidadeEstoque - quantidadeComprada;
        System.out.println("SUCESSO: Venda de " + quantidadeComprada + "x " + this.nome + " realizada. Estoque restante: " + this.quantidadeEstoque);
    }

    public void comprarProduto(int quantidadeAbastecida) {
        if (quantidadeAbastecida <= 0) {
            System.out.println("ERRO: A quantidade de abastecimento deve ser no mínimo 1.");
            return;
        }
        
        this.quantidadeEstoque = this.quantidadeEstoque + quantidadeAbastecida;
        System.out.println("SUCESSO: Estoque de " + this.nome + " abastecido. Novo total: " + this.quantidadeEstoque);
    }

    // Substituindop o "mostrarDados()"

    @Override
    public String toString() {
        return "Produto [ID: " + id + " | Nome: " + nome + " | Preço: R$" + preco + 
               " | Estoque: " + quantidadeEstoque + " | " + categoria.toString() + "]";
    }
}