package com.lojaagro.estoque_api.entities;

import jakarta.persistence.*;
import java.util.UUID;

/** Recibo técnico: gravado na mesma transação da venda/reposição, sem dados pessoais. */
@Entity
public class OperacaoEstoque {
    @Id
    private UUID id;
    @Column(nullable = false, length = 200)
    private String assinatura;

    protected OperacaoEstoque() {}
    public OperacaoEstoque(UUID id, String assinatura) { this.id = id; this.assinatura = assinatura; }
    public String getAssinatura() { return assinatura; }
}
