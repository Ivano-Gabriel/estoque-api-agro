package com.lojaagro.estoque_api.entities;

public enum UsuarioRole {
    ADMIN,
    FUNCIONARIA,
    // Mantido temporariamente para ler usuários antigos já salvos no banco.
    // Novas contas devem usar FUNCIONARIA.
    OPERADOR
}
