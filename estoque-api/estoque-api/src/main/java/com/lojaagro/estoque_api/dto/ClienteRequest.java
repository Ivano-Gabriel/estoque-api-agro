package com.lojaagro.estoque_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record ClienteRequest(
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 25) String telefone,
        @Email @Size(max = 150) String email,
        @Size(max = 500) String observacoes,
        @Size(max = 50) Set<Long> produtoFavoritoIds) {
}
