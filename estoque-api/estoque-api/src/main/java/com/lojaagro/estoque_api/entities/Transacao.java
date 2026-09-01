package com.lojaagro.estoque_api.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Transacao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    
    @Column(nullable = false)
    private String tipo; // "VENDA" ou "COMPRA"
    
    @Column(nullable = false)
    private int quantidade;
    
    @Column(nullable = false)
    private double precoUnitario;
    
    @Column(nullable = false)
    private double valorTotal;
    
    @Column(nullable = false)
    private LocalDateTime data;
    
    private String descricao;
    
    // Construtores
    public Transacao() {}
    
    public Transacao(Produto produto, Usuario usuario, String tipo, int quantidade, 
                     double precoUnitario, double valorTotal, String descricao) {
        this.produto = produto;
        this.usuario = usuario;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.valorTotal = valorTotal;
        this.data = LocalDateTime.now();
        this.descricao = descricao;
    }
    
    // Getters
    public Long getId() { return id; }
    public Produto getProduto() { return produto; }
    public Usuario getUsuario() { return usuario; }
    public String getTipo() { return tipo; }
    public int getQuantidade() { return quantidade; }
    public double getPrecoUnitario() { return precoUnitario; }
    public double getValorTotal() { return valorTotal; }
    public LocalDateTime getData() { return data; }
    public String getDescricao() { return descricao; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public void setPrecoUnitario(double precoUnitario) { this.precoUnitario = precoUnitario; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }
    public void setData(LocalDateTime data) { this.data = data; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}