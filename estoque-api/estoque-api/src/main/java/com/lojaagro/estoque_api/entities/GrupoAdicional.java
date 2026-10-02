package com.lojaagro.estoque_api.entities;
import jakarta.persistence.*;import java.util.*;
@Entity @Table(name="grupo_adicional")
public class GrupoAdicional {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="loja_id",nullable=false) private Loja loja;
 @Column(nullable=false,length=80) private String nome;
 @Column(nullable=false) private int minimo; @Column(nullable=false) private int maximo=1;
 @Column(nullable=false) private boolean obrigatorio; @Column(nullable=false) private boolean ativo=true; @Column(nullable=false) private int ordem;
 @Version @Column(nullable=false) private long versao;
 @OneToMany(mappedBy="grupo",cascade=CascadeType.ALL,orphanRemoval=true) @OrderBy("ordem ASC,id ASC") private List<OpcaoAdicional> opcoes=new ArrayList<>();
 protected GrupoAdicional(){} public GrupoAdicional(Loja loja){this.loja=loja;}
 public void configurar(String nome,int minimo,int maximo,boolean obrigatorio,boolean ativo,int ordem){if(nome==null||nome.isBlank()||nome.trim().length()>80)throw new IllegalArgumentException("Nome do grupo é obrigatório.");if(minimo<0||maximo<1||maximo<minimo)throw new IllegalArgumentException("Limites do grupo são inválidos.");this.nome=nome.trim();this.minimo=obrigatorio?Math.max(1,minimo):minimo;this.maximo=maximo;this.obrigatorio=obrigatorio;this.ativo=ativo;this.ordem=ordem;}
 public void sincronizarOpcoes(List<OpcaoAdicional> novas){Set<OpcaoAdicional>mantidas=Collections.newSetFromMap(new IdentityHashMap<>());mantidas.addAll(novas);opcoes.stream().filter(o->!mantidas.contains(o)).forEach(OpcaoAdicional::desativar);novas.stream().filter(o->!opcoes.contains(o)).forEach(opcoes::add);}
 public Long getId(){return id;}public Loja getLoja(){return loja;}public String getNome(){return nome;}public int getMinimo(){return minimo;}public int getMaximo(){return maximo;}public boolean isObrigatorio(){return obrigatorio;}public boolean isAtivo(){return ativo;}public int getOrdem(){return ordem;}public List<OpcaoAdicional> getOpcoes(){return opcoes;}
}
