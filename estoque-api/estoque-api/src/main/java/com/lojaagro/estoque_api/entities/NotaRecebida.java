package com.lojaagro.estoque_api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nota_recebida",
        indexes = @Index(name = "idx_nota_recebida_loja_data", columnList = "loja_id,data_recebimento"),
        uniqueConstraints = @UniqueConstraint(name = "uk_nota_loja_chave", columnNames = {"loja_id", "chave_acesso"}))
public class NotaRecebida {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loja_id", nullable = false)
    @JsonIgnore
    private Loja loja;
    @Column(nullable = false, length = 120)
    private String fornecedor;
    @Column(length = 14)
    private String documentoFornecedor;
    @Column(nullable = false, length = 30)
    private String numero;
    @Column(length = 10)
    private String serie;
    @Column(name = "chave_acesso", length = 44)
    private String chaveAcesso;
    private LocalDate dataEmissao;
    @Column(name = "data_recebimento", nullable = false)
    private LocalDate dataRecebimento;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorTotal;
    @Column(nullable = false)
    private boolean conferida;
    @Column(nullable = false)
    private boolean estoqueAtualizado;
    @Column(length = 1000)
    private String observacoes;
    @Column(nullable = false)
    private LocalDateTime criadaEm;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    private Usuario usuario;
    @OneToMany(mappedBy = "nota", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotaRecebidaItem> itens = new ArrayList<>();

    protected NotaRecebida() {}

    public NotaRecebida(Loja loja, Usuario usuario, String fornecedor, String documentoFornecedor,
                        String numero, String serie, String chaveAcesso, LocalDate dataEmissao,
                        LocalDate dataRecebimento, BigDecimal valorTotal, boolean conferida,
                        boolean estoqueAtualizado, String observacoes, LocalDateTime criadaEm) {
        this.loja = loja; this.usuario = usuario; this.fornecedor = fornecedor.trim();
        this.documentoFornecedor = documentoFornecedor; this.numero = numero.trim();
        this.serie = vazioParaNulo(serie); this.chaveAcesso = vazioParaNulo(chaveAcesso);
        this.dataEmissao = dataEmissao; this.dataRecebimento = dataRecebimento;
        this.valorTotal = valorTotal.setScale(2, RoundingMode.HALF_UP);
        this.conferida = conferida; this.estoqueAtualizado = estoqueAtualizado;
        this.observacoes = vazioParaNulo(observacoes); this.criadaEm = criadaEm;
    }

    private String vazioParaNulo(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
    public void adicionarItem(NotaRecebidaItem item) { itens.add(item); }
    public void setConferida(boolean conferida) { this.conferida = conferida; }
    public Long getId() { return id; }
    public Loja getLoja() { return loja; }
    public String getFornecedor() { return fornecedor; }
    public String getDocumentoFornecedor() { return documentoFornecedor; }
    public String getNumero() { return numero; }
    public String getSerie() { return serie; }
    public String getChaveAcesso() { return chaveAcesso; }
    public LocalDate getDataEmissao() { return dataEmissao; }
    public LocalDate getDataRecebimento() { return dataRecebimento; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public boolean isConferida() { return conferida; }
    public boolean isEstoqueAtualizado() { return estoqueAtualizado; }
    public String getObservacoes() { return observacoes; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public Usuario getUsuario() { return usuario; }
    public List<NotaRecebidaItem> getItens() { return itens; }
}
