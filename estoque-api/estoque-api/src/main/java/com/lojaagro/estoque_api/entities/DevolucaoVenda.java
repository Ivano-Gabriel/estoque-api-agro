package com.lojaagro.estoque_api.entities;
import jakarta.persistence.*;
import java.math.*; import java.time.LocalDateTime; import java.util.*;
@Entity @Table(name="devolucao_venda",indexes=@Index(name="idx_devolucao_loja_data",columnList="loja_id,criada_em"))
public class DevolucaoVenda{
 @Id private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="loja_id",nullable=false) private Loja loja;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="venda_id",nullable=false) private Venda venda;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="usuario_id",nullable=false) private Usuario usuario;
 @Enumerated(EnumType.STRING) @Column(name="forma_reembolso",nullable=false,length=30) private FormaPagamento formaReembolso;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal valor;
 @Column(nullable=false,length=300) private String motivo;
 @Column(name="criada_em",nullable=false) private LocalDateTime criadaEm;
 @OneToMany(mappedBy="devolucao",cascade=CascadeType.ALL,orphanRemoval=true) private List<DevolucaoVendaItem> itens=new ArrayList<>();
 protected DevolucaoVenda(){}
 public DevolucaoVenda(Loja l,Venda v,Usuario u,FormaPagamento f,String m,LocalDateTime c){id=UUID.randomUUID();loja=l;venda=v;usuario=u;formaReembolso=f;motivo=m.trim();criadaEm=c;valor=BigDecimal.ZERO.setScale(2);}
 public void adicionar(DevolucaoVendaItem i){itens.add(i);valor=valor.add(i.getValor()).setScale(2,RoundingMode.HALF_UP);}
 public UUID getId(){return id;} public Venda getVenda(){return venda;} public FormaPagamento getFormaReembolso(){return formaReembolso;}
 public BigDecimal getValor(){return valor;} public String getMotivo(){return motivo;} public LocalDateTime getCriadaEm(){return criadaEm;} public List<DevolucaoVendaItem> getItens(){return itens;}
}
