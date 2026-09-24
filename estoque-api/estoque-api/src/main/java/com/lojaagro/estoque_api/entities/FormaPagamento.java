package com.lojaagro.estoque_api.entities;

public enum FormaPagamento {
    DINHEIRO("Dinheiro"),
    PIX("PIX"),
    CARTAO_DEBITO("Cartão de débito"),
    CARTAO_CREDITO("Cartão de crédito"),
    OUTRO("Outro"),
    NAO_INFORMADO("Não informado");

    private final String rotulo;

    FormaPagamento(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
