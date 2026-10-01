package com.lojaagro.estoque_api.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "movimento_caixa", indexes = @Index(name = "idx_movimento_caixa_sessao_data", columnList = "sessao_id,criado_em"))
public class MovimentoCaixa {
    @Id private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="sessao_id",nullable=false) private CaixaSessao sessao;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="usuario_id",nullable=false) private Usuario usuario;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="venda_id") private Venda venda;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private TipoMovimentoCaixa tipo;
    @Column(nullable=false,precision=19,scale=2) private BigDecimal valor;
    @Column(length=300) private String descricao;
    @Column(name="criado_em",nullable=false) private LocalDateTime criadoEm;
    protected MovimentoCaixa() {}
    public MovimentoCaixa(CaixaSessao sessao, Usuario usuario, Venda venda, TipoMovimentoCaixa tipo,
                           BigDecimal valor, String descricao, LocalDateTime criadoEm) {
        this.id=UUID.randomUUID(); this.sessao=sessao; this.usuario=usuario; this.venda=venda; this.tipo=tipo;
        this.valor=valor.setScale(2, RoundingMode.HALF_UP); this.descricao=descricao; this.criadoEm=criadoEm;
    }
    public UUID getId(){return id;} public TipoMovimentoCaixa getTipo(){return tipo;} public BigDecimal getValor(){return valor;}
    public String getDescricao(){return descricao;} public LocalDateTime getCriadoEm(){return criadoEm;}
}
