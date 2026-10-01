package com.lojaagro.estoque_api.entities;
import jakarta.persistence.*; import java.math.*;
@Entity @Table(name="devolucao_venda_item")
public class DevolucaoVendaItem{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="devolucao_id",nullable=false) private DevolucaoVenda devolucao;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="venda_item_id",nullable=false) private VendaItem vendaItem;
 @Column(nullable=false) private int quantidade;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal valor;
 protected DevolucaoVendaItem(){}
 public DevolucaoVendaItem(DevolucaoVenda d,VendaItem i,int q,BigDecimal v){devolucao=d;vendaItem=i;quantidade=q;valor=v.setScale(2,RoundingMode.HALF_UP);}
 public VendaItem getVendaItem(){return vendaItem;} public int getQuantidade(){return quantidade;} public BigDecimal getValor(){return valor;}
}
