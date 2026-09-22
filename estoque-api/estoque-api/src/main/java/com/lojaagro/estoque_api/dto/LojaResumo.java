package com.lojaagro.estoque_api.dto;

import com.lojaagro.estoque_api.entities.Loja;

public record LojaResumo(Long id, String nome, String slug, boolean financeiroAtivo, boolean fotosAtivas) {
    public static LojaResumo from(Loja loja) {
        return loja == null ? null : new LojaResumo(
                loja.getId(), loja.getNome(), loja.getSlug(), loja.isFinanceiroAtivo(), loja.isFotosAtivas());
    }
}
