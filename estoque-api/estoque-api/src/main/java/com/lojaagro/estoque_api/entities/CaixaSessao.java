package com.lojaagro.estoque_api.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "caixa_sessao", indexes = {
        @Index(name = "idx_caixa_sessao_loja_abertura", columnList = "loja_id,aberta_em"),
        @Index(name = "idx_caixa_sessao_operador_status", columnList = "operador_id,status")
})
public class CaixaSessao {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "loja_id", nullable = false)
    private Loja loja;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "operador_id", nullable = false)
    private Usuario operador;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private StatusCaixa status = StatusCaixa.ABERTO;
    @Column(name = "aberta_em", nullable = false) private LocalDateTime abertaEm;
    @Column(name = "fechada_em") private LocalDateTime fechadaEm;
    @Column(name = "saldo_inicial", nullable = false, precision = 19, scale = 2) private BigDecimal saldoInicial;
    @Column(name = "total_vendas", nullable = false, precision = 19, scale = 2) private BigDecimal totalVendas = zero();
    @Column(name = "total_dinheiro", nullable = false, precision = 19, scale = 2) private BigDecimal totalDinheiro = zero();
    @Column(name = "total_suprimentos", nullable = false, precision = 19, scale = 2) private BigDecimal totalSuprimentos = zero();
    @Column(name = "total_sangrias", nullable = false, precision = 19, scale = 2) private BigDecimal totalSangrias = zero();
    @Column(name = "total_estornos", nullable = false, precision = 19, scale = 2) private BigDecimal totalEstornos = zero();
    @Column(name = "total_estornos_dinheiro", nullable = false, precision = 19, scale = 2) private BigDecimal totalEstornosDinheiro = zero();
    @Column(name = "saldo_esperado", precision = 19, scale = 2) private BigDecimal saldoEsperado;
    @Column(name = "saldo_informado", precision = 19, scale = 2) private BigDecimal saldoInformado;
    @Column(precision = 19, scale = 2) private BigDecimal diferenca;
    @Column(length = 500) private String observacoes;
    @Version private long versao;

    protected CaixaSessao() {}
    public CaixaSessao(Loja loja, Usuario operador, BigDecimal saldoInicial, LocalDateTime momento) {
        this.id = UUID.randomUUID(); this.loja = loja; this.operador = operador;
        this.saldoInicial = normalizar(saldoInicial); this.abertaEm = momento;
    }
    public void registrarVenda(BigDecimal total, BigDecimal dinheiro) { exigirAberto(); totalVendas = totalVendas.add(normalizar(total)); totalDinheiro = totalDinheiro.add(normalizar(dinheiro)); }
    public void registrarSuprimento(BigDecimal valor) { exigirAberto(); totalSuprimentos = totalSuprimentos.add(positivo(valor)); }
    public void registrarSangria(BigDecimal valor) {
        exigirAberto(); BigDecimal normalizado = positivo(valor);
        if (normalizado.compareTo(saldoAtual()) > 0) throw new IllegalArgumentException("Sangria maior que o saldo esperado do caixa.");
        totalSangrias = totalSangrias.add(normalizado);
    }
    public void registrarEstorno(BigDecimal total, BigDecimal dinheiro) { exigirAberto(); totalEstornos = totalEstornos.add(positivo(total)); totalEstornosDinheiro = totalEstornosDinheiro.add(normalizar(dinheiro)); }
    public void fechar(BigDecimal informado, String observacoes, LocalDateTime momento) {
        exigirAberto(); saldoEsperado = saldoAtual(); saldoInformado = normalizar(informado);
        diferenca = saldoInformado.subtract(saldoEsperado).setScale(2, RoundingMode.HALF_UP);
        this.observacoes = observacoes == null || observacoes.isBlank() ? null : observacoes.trim();
        fechadaEm = momento; status = StatusCaixa.FECHADO;
    }
    public BigDecimal saldoAtual() { return saldoInicial.add(totalDinheiro).add(totalSuprimentos)
            .subtract(totalSangrias).subtract(totalEstornosDinheiro).setScale(2, RoundingMode.HALF_UP); }
    private void exigirAberto() { if (status != StatusCaixa.ABERTO) throw new IllegalArgumentException("Caixa já fechado."); }
    private BigDecimal positivo(BigDecimal v) { BigDecimal n=normalizar(v); if(n.signum()<=0) throw new IllegalArgumentException("Valor deve ser maior que zero."); return n; }
    private BigDecimal normalizar(BigDecimal v) { return (v==null?BigDecimal.ZERO:v).setScale(2, RoundingMode.HALF_UP); }
    private static BigDecimal zero() { return BigDecimal.ZERO.setScale(2); }
    public UUID getId(){return id;} public Loja getLoja(){return loja;} public Usuario getOperador(){return operador;}
    public StatusCaixa getStatus(){return status;} public LocalDateTime getAbertaEm(){return abertaEm;}
    public LocalDateTime getFechadaEm(){return fechadaEm;} public BigDecimal getSaldoInicial(){return saldoInicial;}
    public BigDecimal getTotalVendas(){return totalVendas;} public BigDecimal getTotalSuprimentos(){return totalSuprimentos;}
    public BigDecimal getTotalDinheiro(){return totalDinheiro;}
    public BigDecimal getTotalSangrias(){return totalSangrias;} public BigDecimal getTotalEstornos(){return totalEstornos;}
    public BigDecimal getSaldoEsperado(){return saldoEsperado;} public BigDecimal getSaldoInformado(){return saldoInformado;}
    public BigDecimal getDiferenca(){return diferenca;} public String getObservacoes(){return observacoes;}
    public BigDecimal getTotalEstornosDinheiro(){return totalEstornosDinheiro;}
}
