package com.lojaagro.estoque_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelarVendaRequest(
        @NotBlank(message = "Informe o motivo do cancelamento.")
        @Size(max = 300, message = "O motivo deve ter no máximo 300 caracteres.") String motivo) {}
