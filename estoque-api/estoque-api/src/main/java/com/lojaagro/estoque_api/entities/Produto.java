package com.lojaagro.estoque_api.entities;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "produto")
// Sempre que buscar produtos, traz apenas os ativos
@SQLRestriction("ativo = true")
public class Produto {

    private String nome;
    private String tipo;
    private double preco; 
   
    private LocalDate dataValidade;

    @ManyToOne(optional = false)
    private Categoria categoria;
    
    public Produto() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @jakarta.persistence.Column(nullable = false, columnDefinition = "bigint default 0")
    private long versao;

    private int quantidadeEstoque = 0;
    
    // NOVO CAMPO: Controla se o produto foi "deletado"
    private boolean ativo = true;

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
    public boolean isAtivo() { return ativo; } // Getter do ativo

    // Setters de Configuração Base
    public void setPreco(double novoPreco) {
        if (novoPreco <= 0) {
            throw new IllegalArgumentException("O preço deve ser maior que zero.");
        }
        this.preco = novoPreco;
    }

    public void setQuantidadeEstoque(int novaQuantidade) {
        if (novaQuantidade < 0) {
            throw new IllegalArgumentException("Estoque não pode ser negativo.");
        }   
        this.quantidadeEstoque = novaQuantidade;
    }
    
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public void atualizarDados(String nome,
                               String tipo,
                               double preco,
                               LocalDate dataValidade,
                               int quantidadeEstoque,
                               Categoria categoria) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("O tipo do produto é obrigatório.");
        }
        if (categoria == null) {
            throw new IllegalArgumentException("A categoria do produto é obrigatória.");
        }

        this.nome = nome.trim();
        this.tipo = tipo.trim();
        setPreco(preco);
        this.dataValidade = dataValidade;
        setQuantidadeEstoque(quantidadeEstoque);
        this.categoria = categoria;
    }

    // MÉTODOS DE NEGÓCIO (A Inteligência)
    public void venderProduto(int quantidadeComprada) {
        if (quantidadeComprada <= 0) {
            throw new IllegalArgumentException("Quantidade de venda deve ser no mínimo 1.");
        }
        if (quantidadeComprada > this.quantidadeEstoque) {
            throw new IllegalArgumentException("Estoque insuficiente para " + this.nome + ".");
        }
        
        this.quantidadeEstoque = this.quantidadeEstoque - quantidadeComprada;
    }

    public void comprarProduto(int quantidadeAbastecida) {
        if (quantidadeAbastecida <= 0) {
            throw new IllegalArgumentException("A quantidade de abastecimento deve ser no mínimo 1.");
        }
        
        this.quantidadeEstoque = this.quantidadeEstoque + quantidadeAbastecida;
    }

    @Override
    public String toString() {
        return "Produto [ID: " + id + " | Nome: " + nome + " | Preço: R$" + preco + 
               " | Estoque: " + quantidadeEstoque + " | " + categoria.toString() + "]";
    }
}
