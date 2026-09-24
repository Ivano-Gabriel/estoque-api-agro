package com.lojaagro.estoque_api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "venda_item")
public class VendaItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venda_id", nullable = false)
    @JsonIgnore
    private Venda venda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    @JsonIgnore
    private Produto produto;

    @Column(name = "nome_produto", nullable = false, length = 120)
    private String nomeProduto;

    @Column(name = "tipo_produto", nullable = false, length = 40)
    private String tipoProduto;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 19, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "custo_unitario", nullable = false, precision = 19, scale = 2)
    private BigDecimal custoUnitario;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "desconto_rateado", nullable = false, precision = 19, scale = 2)
    private BigDecimal descontoRateado;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    protected VendaItem() {}

    public VendaItem(Venda venda, Produto produto, int quantidade, BigDecimal precoUnitario,
                     BigDecimal custoUnitario, BigDecimal subtotal, BigDecimal descontoRateado,
                     BigDecimal total) {
        this.venda = venda;
        this.produto = produto;
        this.nomeProduto = produto.getNome();
        this.tipoProduto = produto.getTipo();
        this.quantidade = quantidade;
        this.precoUnitario = normalizar(precoUnitario);
        this.custoUnitario = normalizar(custoUnitario);
        this.subtotal = normalizar(subtotal);
        this.descontoRateado = normalizar(descontoRateado);
        this.total = normalizar(total);
    }

    private BigDecimal normalizar(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public Venda getVenda() { return venda; }
    public Produto getProduto() { return produto; }
    public String getNomeProduto() { return nomeProduto; }
    public String getTipoProduto() { return tipoProduto; }
    public int getQuantidade() { return quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public BigDecimal getCustoUnitario() { return custoUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDescontoRateado() { return descontoRateado; }
    public BigDecimal getTotal() { return total; }
}
