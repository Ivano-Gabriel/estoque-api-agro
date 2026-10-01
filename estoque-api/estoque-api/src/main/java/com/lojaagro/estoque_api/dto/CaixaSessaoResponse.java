package com.lojaagro.estoque_api.dto;

import com.lojaagro.estoque_api.entities.CaixaSessao;
import com.lojaagro.estoque_api.entities.StatusCaixa;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CaixaSessaoResponse(UUID id, String operador, StatusCaixa status,
        LocalDateTime abertaEm, LocalDateTime fechadaEm, BigDecimal saldoInicial,
        BigDecimal totalVendas, BigDecimal totalDinheiro, BigDecimal totalSuprimentos, BigDecimal totalSangrias,
        BigDecimal totalEstornos, BigDecimal totalEstornosDinheiro, BigDecimal saldoAtual, BigDecimal saldoEsperado,
        BigDecimal saldoInformado, BigDecimal diferenca, String observacoes) {
    public static CaixaSessaoResponse de(CaixaSessao c) {
        return new CaixaSessaoResponse(c.getId(), c.getOperador().getEmail(), c.getStatus(),
                c.getAbertaEm(), c.getFechadaEm(), c.getSaldoInicial(), c.getTotalVendas(), c.getTotalDinheiro(),
                c.getTotalSuprimentos(), c.getTotalSangrias(), c.getTotalEstornos(), c.getTotalEstornosDinheiro(),
                c.saldoAtual(), c.getSaldoEsperado(), c.getSaldoInformado(), c.getDiferenca(), c.getObservacoes());
    }
}
