package com.lojaagro.estoque_api.dto;

import com.lojaagro.estoque_api.entities.UsuarioRole;

public record LoginResponse(
        String token,
        String email,
        UsuarioRole role,
        LojaResumo loja) {
}
