package com.lojaagro.estoque_api.dto;
import com.lojaagro.estoque_api.entities.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
public record MovimentoCaixaResponse(UUID id, TipoMovimentoCaixa tipo, BigDecimal valor,
                                      String descricao, LocalDateTime criadoEm) {
    public static MovimentoCaixaResponse de(MovimentoCaixa m) {
        return new MovimentoCaixaResponse(m.getId(), m.getTipo(), m.getValor(), m.getDescricao(), m.getCriadoEm());
    }
}
