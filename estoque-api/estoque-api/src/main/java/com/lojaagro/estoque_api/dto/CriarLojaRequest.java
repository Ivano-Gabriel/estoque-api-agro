package com.lojaagro.estoque_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CriarLojaRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotBlank @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 60) String slug,
        @NotNull Boolean financeiroAtivo,
        Boolean fotosAtivas,
        @Size(max = 20) String whatsapp,
        @NotBlank @Email @Size(max = 150) String adminEmail,
        @NotBlank @Size(min = 10, max = 72) String adminSenha) {}
