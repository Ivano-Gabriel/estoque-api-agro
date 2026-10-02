package com.lojaagro.estoque_api.entities;
import jakarta.persistence.*;
@Entity @Table(name="ficha_tecnica_item",uniqueConstraints=@UniqueConstraint(name="uk_ficha_item_ingrediente",columnNames={"item_cardapio_id","ingrediente_id"}))
public class FichaTecnicaItem {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="item_cardapio_id",nullable=false) private ItemCardapio item;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="ingrediente_id",nullable=false) private Produto ingrediente;
 @Column(nullable=false) private int quantidade;
 protected FichaTecnicaItem(){}
 public FichaTecnicaItem(ItemCardapio item,Produto ingrediente,int quantidade){if(quantidade<=0)throw new IllegalArgumentException("Quantidade do ingrediente deve ser positiva.");this.item=item;this.ingrediente=ingrediente;this.quantidade=quantidade;}
 public Produto getIngrediente(){return ingrediente;} public int getQuantidade(){return quantidade;}
}
