package com.lojaagro.estoque_api.dto;

import com.lojaagro.estoque_api.entities.UsuarioRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarUsuarioRequest(
        @Email @NotBlank @Size(max = 254) String email,
        @NotBlank @Size(min = 10, max = 72) String senha,
        @NotNull UsuarioRole role) {
}
