package com.lojaagro.estoque_api.entities;
import jakarta.persistence.*;
@Entity @Table(name="opcao_adicional",uniqueConstraints=@UniqueConstraint(name="uk_opcao_grupo_produto",columnNames={"grupo_id","produto_id"}))
public class OpcaoAdicional {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="grupo_id",nullable=false) private GrupoAdicional grupo;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="produto_id",nullable=false) private Produto produto;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ingrediente_id") private Produto ingrediente;
 @Column(name="quantidade_insumo",nullable=false) private int quantidadeInsumo;
 @Column(nullable=false) private boolean ativo=true;@Column(nullable=false) private int ordem;
 protected OpcaoAdicional(){}
 public OpcaoAdicional(GrupoAdicional grupo,Produto produto,Produto ingrediente,int quantidadeInsumo,boolean ativo,int ordem){if(quantidadeInsumo<0)throw new IllegalArgumentException("Consumo do adicional não pode ser negativo.");this.grupo=grupo;this.produto=produto;this.ingrediente=ingrediente;this.quantidadeInsumo=quantidadeInsumo;this.ativo=ativo;this.ordem=ordem;}
 public void configurar(Produto ingrediente,int quantidadeInsumo,boolean ativo,int ordem){if(quantidadeInsumo<0)throw new IllegalArgumentException("Consumo do adicional não pode ser negativo.");this.ingrediente=ingrediente;this.quantidadeInsumo=quantidadeInsumo;this.ativo=ativo;this.ordem=ordem;}
 public void desativar(){this.ativo=false;}
 public Long getId(){return id;}public GrupoAdicional getGrupo(){return grupo;}public Produto getProduto(){return produto;}public Produto getIngrediente(){return ingrediente;}public int getQuantidadeInsumo(){return quantidadeInsumo;}public boolean isAtivo(){return ativo;}public int getOrdem(){return ordem;}
}
