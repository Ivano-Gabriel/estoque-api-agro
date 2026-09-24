package com.lojaagro.estoque_api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "nota_recebida_item")
public class NotaRecebidaItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nota_id", nullable = false)
    @JsonIgnore
    private NotaRecebida nota;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;
    @Column(nullable = false)
    private int quantidade;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal custoUnitario;

    protected NotaRecebidaItem() {}
    public NotaRecebidaItem(NotaRecebida nota, Produto produto, int quantidade, BigDecimal custoUnitario) {
        this.nota = nota; this.produto = produto; this.quantidade = quantidade;
        this.custoUnitario = custoUnitario.setScale(2, RoundingMode.HALF_UP);
    }
    public Long getId() { return id; }
    public Produto getProduto() { return produto; }
    public int getQuantidade() { return quantidade; }
    public BigDecimal getCustoUnitario() { return custoUnitario; }
}
