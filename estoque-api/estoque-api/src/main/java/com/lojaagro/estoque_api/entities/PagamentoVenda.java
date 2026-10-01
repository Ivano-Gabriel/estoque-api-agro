package com.lojaagro.estoque_api.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name="pagamento_venda", indexes=@Index(name="idx_pagamento_venda_venda",columnList="venda_id"))
public class PagamentoVenda {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="venda_id",nullable=false) private Venda venda;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private FormaPagamento forma;
    @Column(nullable=false,precision=19,scale=2) private BigDecimal valor;
    protected PagamentoVenda(){}
    public PagamentoVenda(Venda venda,FormaPagamento forma,BigDecimal valor){this.venda=venda;this.forma=forma;this.valor=valor.setScale(2, RoundingMode.HALF_UP);}
    public Long getId(){return id;} public FormaPagamento getForma(){return forma;} public BigDecimal getValor(){return valor;}
}
