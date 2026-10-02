package com.lojaagro.estoque_api.entities;
import jakarta.persistence.*;
@Entity @Table(name="mesa_lanchonete",uniqueConstraints=@UniqueConstraint(name="uk_mesa_loja_nome",columnNames={"loja_id","nome"}))
public class MesaLanchonete {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;@ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="loja_id",nullable=false)private Loja loja;
 @Column(nullable=false,length=40)private String nome;@Column(nullable=false)private int lugares=4;@Column(nullable=false)private boolean ativa=true;@Column(nullable=false)private int ordem;@Version@Column(nullable=false)private long versao;
 protected MesaLanchonete(){}public MesaLanchonete(Loja loja){this.loja=loja;}public void configurar(String nome,int lugares,boolean ativa,int ordem){if(nome==null||nome.isBlank()||nome.trim().length()>40)throw new IllegalArgumentException("Nome da mesa é obrigatório.");if(lugares<1||lugares>100)throw new IllegalArgumentException("Quantidade de lugares inválida.");this.nome=nome.trim();this.lugares=lugares;this.ativa=ativa;this.ordem=ordem;}
 public Long getId(){return id;}public Loja getLoja(){return loja;}public String getNome(){return nome;}public int getLugares(){return lugares;}public boolean isAtiva(){return ativa;}public int getOrdem(){return ordem;}
}
