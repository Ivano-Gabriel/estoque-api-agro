package com.lojaagro.estoque_api.entities;
import jakarta.persistence.*;
import java.util.*;

@Entity @Table(name="item_cardapio",uniqueConstraints=@UniqueConstraint(name="uk_item_cardapio_loja_produto",columnNames={"loja_id","produto_id"}))
public class ItemCardapio {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="loja_id",nullable=false) private Loja loja;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="produto_id",nullable=false) private Produto produto;
 @Column(name="nome_cozinha",length=80) private String nomeCozinha;
 @Column(nullable=false,length=40) private String estacao="COZINHA";
 @Column(name="tempo_preparo_minutos",nullable=false) private int tempoPreparoMinutos=15;
 @Column(nullable=false) private boolean disponivel=true;
 @Column(nullable=false) private boolean destaque=false;
 @Column(name="baixa_produto_final",nullable=false) private boolean baixaProdutoFinal=false;
 @Column(nullable=false) private int ordem=0;
 @Version @Column(nullable=false) private long versao;
 @OneToMany(mappedBy="item",cascade=CascadeType.ALL,orphanRemoval=true) @OrderBy("id ASC") private List<FichaTecnicaItem> ingredientes=new ArrayList<>();
 @ManyToMany @JoinTable(name="item_cardapio_grupo",joinColumns=@JoinColumn(name="item_cardapio_id"),inverseJoinColumns=@JoinColumn(name="grupo_id"))
 @OrderBy("ordem ASC,id ASC") private Set<GrupoAdicional> grupos=new LinkedHashSet<>();
 protected ItemCardapio(){}
 public ItemCardapio(Loja loja,Produto produto){this.loja=loja;this.produto=produto;}
 public void configurar(String nomeCozinha,String estacao,int tempo,boolean disponivel,boolean destaque,boolean baixaProdutoFinal,int ordem){
  if(tempo<0||tempo>480)throw new IllegalArgumentException("Tempo de preparo inválido.");
  this.nomeCozinha=texto(nomeCozinha,80);this.estacao=textoObrigatorio(estacao,"Estação",40).toUpperCase(Locale.ROOT);
  this.tempoPreparoMinutos=tempo;this.disponivel=disponivel;this.destaque=destaque;this.baixaProdutoFinal=baixaProdutoFinal;this.ordem=ordem;
 }
 public void substituirIngredientes(List<FichaTecnicaItem> novos){ingredientes.clear();ingredientes.addAll(novos);}
 public void substituirGrupos(Collection<GrupoAdicional> novos){grupos.clear();grupos.addAll(novos);}
 private String texto(String v,int max){if(v==null||v.isBlank())return null;String s=v.trim();if(s.length()>max)throw new IllegalArgumentException("Texto acima do limite.");return s;}
 private String textoObrigatorio(String v,String campo,int max){String s=texto(v,max);if(s==null)throw new IllegalArgumentException(campo+" é obrigatória.");return s;}
 public Long getId(){return id;} public Loja getLoja(){return loja;} public Produto getProduto(){return produto;}
 public String getNomeCozinha(){return nomeCozinha;} public String getEstacao(){return estacao;} public int getTempoPreparoMinutos(){return tempoPreparoMinutos;}
 public boolean isDisponivel(){return disponivel;} public boolean isDestaque(){return destaque;} public boolean isBaixaProdutoFinal(){return baixaProdutoFinal;} public int getOrdem(){return ordem;}
 public List<FichaTecnicaItem> getIngredientes(){return ingredientes;} public Set<GrupoAdicional> getGrupos(){return grupos;}
}
