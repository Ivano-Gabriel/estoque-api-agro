package com.lojaagro.estoque_api.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
public class Transacao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loja_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Loja loja;
    
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
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal precoUnitario;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorTotal;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2,
            columnDefinition = "numeric(19,2) default 0.00")
    private BigDecimal custoUnitario = BigDecimal.ZERO.setScale(2);

    @Column(
            nullable = false,
            precision = 19,
            scale = 2,
            columnDefinition = "numeric(19,2) default 0.00")
    private BigDecimal lucro = BigDecimal.ZERO.setScale(2);
    
    @Column(nullable = false)
    private LocalDateTime data;
    
    private String descricao;
    
    // Construtores
    public Transacao() {}
    
    public Transacao(Produto produto, Usuario usuario, String tipo, int quantidade, 
                     BigDecimal precoUnitario, BigDecimal valorTotal, String descricao) {
        this.produto = produto;
        this.usuario = usuario;
        this.tipo = tipo;
        this.quantidade = quantidade;
        setPrecoUnitario(precoUnitario);
        setValorTotal(valorTotal);
        this.data = LocalDateTime.now();
        this.descricao = descricao;
    }
    
    // Getters
    public Long getId() { return id; }
    public Produto getProduto() { return produto; }
    public Usuario getUsuario() { return usuario; }
    public String getTipo() { return tipo; }
    public int getQuantidade() { return quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public BigDecimal getCustoUnitario() { return custoUnitario; }
    public BigDecimal getLucro() { return lucro; }
    public LocalDateTime getData() { return data; }
    public String getDescricao() { return descricao; }
    public Loja getLoja() { return loja; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario.setScale(2, RoundingMode.HALF_UP);
    }
    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal.setScale(2, RoundingMode.HALF_UP);
    }
    public void setCustoUnitario(BigDecimal custoUnitario) {
        this.custoUnitario = normalizar(custoUnitario);
    }
    public void setLucro(BigDecimal lucro) {
        this.lucro = normalizar(lucro);
    }
    public void setData(LocalDateTime data) { this.data = data; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setLoja(Loja loja) { this.loja = loja; }

    private BigDecimal normalizar(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }
}
