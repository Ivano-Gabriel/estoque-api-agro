package com.lojaagro.estoque_api.dto;

public record ImportacaoPlanilhaErro(
        int linha,
        String campo,
        String mensagem) {
}
