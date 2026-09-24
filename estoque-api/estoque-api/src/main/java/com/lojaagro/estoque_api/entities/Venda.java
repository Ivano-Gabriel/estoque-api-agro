package com.lojaagro.estoque_api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "venda", indexes = {
        @Index(name = "idx_venda_loja_data", columnList = "loja_id,criada_em"),
        @Index(name = "idx_venda_loja_status", columnList = "loja_id,status")
})
public class Venda {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loja_id", nullable = false)
    @JsonIgnore
    private Loja loja;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @JsonIgnore
    private Cliente cliente;

    @Column(nullable = false, length = 64)
    private String assinatura;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 30)
    private FormaPagamento formaPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusVenda status = StatusVenda.CONCLUIDA;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal desconto;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @Column(name = "valor_recebido", precision = 19, scale = 2)
    private BigDecimal valorRecebido;

    @Column(precision = 19, scale = 2)
    private BigDecimal troco;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    @Column(name = "cancelada_em")
    private LocalDateTime canceladaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelada_por_id")
    @JsonIgnore
    private Usuario canceladaPor;

    @Column(name = "motivo_cancelamento", length = 300)
    private String motivoCancelamento;

    @Version
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long versao;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<VendaItem> itens = new ArrayList<>();

    protected Venda() {}

    public Venda(UUID id, Loja loja, Usuario usuario, Cliente cliente, String assinatura,
                 FormaPagamento formaPagamento, BigDecimal subtotal, BigDecimal desconto,
                 BigDecimal total, BigDecimal valorRecebido, BigDecimal troco, LocalDateTime criadaEm) {
        this.id = id;
        this.loja = loja;
        this.usuario = usuario;
        this.cliente = cliente;
        this.assinatura = assinatura;
        this.formaPagamento = formaPagamento;
        this.subtotal = normalizar(subtotal);
        this.desconto = normalizar(desconto);
        this.total = normalizar(total);
        this.valorRecebido = valorRecebido == null ? null : normalizar(valorRecebido);
        this.troco = troco == null ? null : normalizar(troco);
        this.criadaEm = criadaEm;
    }

    public void adicionarItem(VendaItem item) { itens.add(item); }

    public void cancelar(Usuario responsavel, String motivo, LocalDateTime momento) {
        if (status == StatusVenda.CANCELADA) throw new IllegalArgumentException("Esta venda já foi cancelada.");
        if (motivo == null || motivo.isBlank()) throw new IllegalArgumentException("Informe o motivo do cancelamento.");
        status = StatusVenda.CANCELADA;
        canceladaPor = responsavel;
        motivoCancelamento = motivo.trim();
        canceladaEm = momento;
    }

    private BigDecimal normalizar(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }

    public UUID getId() { return id; }
    public Loja getLoja() { return loja; }
    public Usuario getUsuario() { return usuario; }
    public Cliente getCliente() { return cliente; }
    public String getAssinatura() { return assinatura; }
    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public StatusVenda getStatus() { return status; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDesconto() { return desconto; }
    public BigDecimal getTotal() { return total; }
    public BigDecimal getValorRecebido() { return valorRecebido; }
    public BigDecimal getTroco() { return troco; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public LocalDateTime getCanceladaEm() { return canceladaEm; }
    public Usuario getCanceladaPor() { return canceladaPor; }
    public String getMotivoCancelamento() { return motivoCancelamento; }
    public List<VendaItem> getItens() { return itens; }
}
