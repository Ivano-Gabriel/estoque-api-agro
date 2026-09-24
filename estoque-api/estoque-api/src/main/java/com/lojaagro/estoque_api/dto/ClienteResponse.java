package com.lojaagro.estoque_api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ClienteResponse(
        Long id,
        String nome,
        String telefone,
        String email,
        String observacoes,
        LocalDateTime criadoEm,
        List<ProdutoResumo> favoritos,
        List<ProdutoComprado> maisComprados) {

    public record ProdutoResumo(Long id, String nome, String categoria) {}
    public record ProdutoComprado(Long id, String nome, long quantidade) {}
}
