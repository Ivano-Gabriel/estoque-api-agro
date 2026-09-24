package com.lojaagro.estoque_api.entities;

import jakarta.persistence.*;
import java.util.UUID;

/** Recibo técnico: gravado na mesma transação da venda/reposição, sem dados pessoais. */
@Entity
public class OperacaoEstoque {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loja_id")
    private Loja loja;
    @Column(nullable = false, length = 200)
    private String assinatura;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transacao_id")
    private Transacao transacao;

    protected OperacaoEstoque() {}
    public OperacaoEstoque(UUID id, String assinatura, Loja loja) {
        this(id, assinatura, loja, null);
    }
    public OperacaoEstoque(UUID id, String assinatura, Loja loja, Transacao transacao) {
        this.id = id; this.assinatura = assinatura; this.loja = loja; this.transacao = transacao;
    }
    public String getAssinatura() { return assinatura; }
    public Loja getLoja() { return loja; }
    public Transacao getTransacao() { return transacao; }
}
