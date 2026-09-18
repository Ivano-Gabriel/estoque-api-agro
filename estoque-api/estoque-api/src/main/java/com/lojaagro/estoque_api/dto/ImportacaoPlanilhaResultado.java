package com.lojaagro.estoque_api.dto;

import java.util.List;

public record ImportacaoPlanilhaResultado(
        int totalImportado,
        int totalLinhas,
        List<ImportacaoPlanilhaErro> erros) {
}
