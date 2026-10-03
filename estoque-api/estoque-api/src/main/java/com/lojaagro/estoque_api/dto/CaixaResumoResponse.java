package com.lojaagro.estoque_api.dto;

import com.lojaagro.estoque_api.entities.FluxoCaixa;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record CaixaResumoResponse(BigDecimal totalEntradas, BigDecimal totalSaidas,
                                  BigDecimal saldoLiquido, BigDecimal lucroBruto,
                                  LocalDateTime ultimaAtualizacao,
                                  Map<String, BigDecimal> recebimentosPorForma) {
    public static CaixaResumoResponse de(FluxoCaixa caixa, BigDecimal lucroBruto,
                                         Map<String, BigDecimal> formas) {
        return new CaixaResumoResponse(caixa.getTotalEntradas(), caixa.getTotalSaidas(),
                caixa.getSaldoLiquido(), lucroBruto, caixa.getUltimaAtualizacao(), formas);
    }
}
