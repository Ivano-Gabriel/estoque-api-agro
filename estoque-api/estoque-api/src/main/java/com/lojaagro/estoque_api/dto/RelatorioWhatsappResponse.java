package com.lojaagro.estoque_api.dto;

public record RelatorioWhatsappResponse(
        String periodo,
        String mensagem,
        String url) {
}
