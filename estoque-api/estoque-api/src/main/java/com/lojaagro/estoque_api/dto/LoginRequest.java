package com.lojaagro.estoque_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email @NotBlank @jakarta.validation.constraints.Size(max = 254) String email,
        @NotBlank @jakarta.validation.constraints.Size(max = 72) String senha,
        @jakarta.validation.constraints.Pattern(regexp = "^$|\\d{6}", message = "Código de segurança inválido.")
        String codigoMfa) {
    public LoginRequest(String email, String senha) { this(email, senha, null); }
}
