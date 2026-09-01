package com.lojaagro.estoque_api.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class FluxoCaixa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private double totalEntradas = 0.0;
    
    @Column(nullable = false)
    private double totalSaidas = 0.0;
    
    @Column(nullable = false)
    private double saldoLiquido = 0.0;
    
    private LocalDateTime ultimaAtualizacao;
    
    public FluxoCaixa() {
        this.ultimaAtualizacao = LocalDateTime.now();
    }
    
    // Métodos de negócio
    public void adicionarEntrada(double valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("Valor da entrada deve ser positivo");
        }
        this.totalEntradas += valor;
        this.saldoLiquido = this.totalEntradas - this.totalSaidas;
        this.ultimaAtualizacao = LocalDateTime.now();
    }
    
    public void adicionarSaida(double valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("Valor da saída deve ser positivo");
        }
        this.totalSaidas += valor;
        this.saldoLiquido = this.totalEntradas - this.totalSaidas;
        this.ultimaAtualizacao = LocalDateTime.now();
    }
    
    // Getters
    public Long getId() { return id; }
    public double getTotalEntradas() { return totalEntradas; }
    public double getTotalSaidas() { return totalSaidas; }
    public double getSaldoLiquido() { return saldoLiquido; }
    public LocalDateTime getUltimaAtualizacao() { return ultimaAtualizacao; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTotalEntradas(double totalEntradas) { 
        this.totalEntradas = totalEntradas; 
        this.saldoLiquido = this.totalEntradas - this.totalSaidas;
    }
    public void setTotalSaidas(double totalSaidas) { 
        this.totalSaidas = totalSaidas; 
        this.saldoLiquido = this.totalEntradas - this.totalSaidas;
    }
    public void setSaldoLiquido(double saldoLiquido) { this.saldoLiquido = saldoLiquido; }
    public void setUltimaAtualizacao(LocalDateTime ultimaAtualizacao) { 
        this.ultimaAtualizacao = ultimaAtualizacao; 
    }
}