package com.lojaagro.estoque_api.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
public class FluxoCaixa {
    
    @Id
    private Long id = 1L;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loja_id", unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Loja loja;

    @Version
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long versao;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalEntradas = BigDecimal.ZERO.setScale(2);
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalSaidas = BigDecimal.ZERO.setScale(2);
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoLiquido = BigDecimal.ZERO.setScale(2);
    
    private LocalDateTime ultimaAtualizacao;
    
    public FluxoCaixa() {
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public FluxoCaixa(Long id, Loja loja) {
        this();
        this.id = id;
        this.loja = loja;
    }
    
    // Métodos de negócio
    public void adicionarEntrada(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da entrada deve ser positivo");
        }
        this.totalEntradas = this.totalEntradas.add(valor).setScale(2, RoundingMode.HALF_UP);
        recalcularSaldo();
        this.ultimaAtualizacao = LocalDateTime.now();
    }
    
    public void adicionarSaida(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da saída deve ser positivo");
        }
        this.totalSaidas = this.totalSaidas.add(valor).setScale(2, RoundingMode.HALF_UP);
        recalcularSaldo();
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void estornarEntrada(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do estorno deve ser positivo");
        }
        if (totalEntradas.compareTo(valor) < 0) {
            throw new IllegalArgumentException("O caixa não possui entradas suficientes para cancelar esta venda.");
        }
        totalEntradas = totalEntradas.subtract(valor).setScale(2, RoundingMode.HALF_UP);
        recalcularSaldo();
        ultimaAtualizacao = LocalDateTime.now();
    }
    
    // Getters
    public Long getId() { return id; }
    public BigDecimal getTotalEntradas() { return totalEntradas; }
    public BigDecimal getTotalSaidas() { return totalSaidas; }
    public BigDecimal getSaldoLiquido() { return saldoLiquido; }
    public LocalDateTime getUltimaAtualizacao() { return ultimaAtualizacao; }
    public Loja getLoja() { return loja; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTotalEntradas(BigDecimal totalEntradas) {
        this.totalEntradas = normalizar(totalEntradas);
        recalcularSaldo();
    }
    public void setTotalSaidas(BigDecimal totalSaidas) {
        this.totalSaidas = normalizar(totalSaidas);
        recalcularSaldo();
    }
    public void setSaldoLiquido(BigDecimal saldoLiquido) { this.saldoLiquido = normalizar(saldoLiquido); }
    public void setUltimaAtualizacao(LocalDateTime ultimaAtualizacao) { 
        this.ultimaAtualizacao = ultimaAtualizacao; 
    }
    public void setLoja(Loja loja) { this.loja = loja; }

    private void recalcularSaldo() {
        this.saldoLiquido = this.totalEntradas.subtract(this.totalSaidas)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizar(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }
}
