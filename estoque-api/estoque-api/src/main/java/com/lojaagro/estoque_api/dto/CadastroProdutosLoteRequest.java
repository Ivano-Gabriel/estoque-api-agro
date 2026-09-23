package com.lojaagro.estoque_api.dto;

import java.util.List;

public record CadastroProdutosLoteRequest(
        List<ProdutoRequest> produtos) {
}
