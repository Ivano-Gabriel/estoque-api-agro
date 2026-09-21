package com.lojaagro.estoque_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email @NotBlank @jakarta.validation.constraints.Size(max = 254) String email,
        @NotBlank @jakarta.validation.constraints.Size(max = 72) String senha) {
}
