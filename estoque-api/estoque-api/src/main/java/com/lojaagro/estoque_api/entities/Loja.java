package com.lojaagro.estoque_api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(name = "loja", uniqueConstraints = @UniqueConstraint(name = "uk_loja_slug", columnNames = "slug"))
public class Loja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 60)
    private String slug;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean ativa = true;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean financeiroAtivo = true;

    @Column(length = 20)
    private String whatsapp;

    @Version
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long versao;

    protected Loja() {}

    public Loja(String nome, String slug, boolean financeiroAtivo, String whatsapp) {
        atualizar(nome, slug, financeiroAtivo, whatsapp);
    }

    public void atualizar(String nome, String slug, boolean financeiroAtivo, String whatsapp) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome da loja é obrigatório.");
        if (slug == null || !slug.matches("[a-z0-9]+(?:-[a-z0-9]+)*")) {
            throw new IllegalArgumentException("Identificador da loja deve usar letras minúsculas, números e hífens.");
        }
        this.nome = nome.trim();
        this.slug = slug.trim();
        this.financeiroAtivo = financeiroAtivo;
        this.whatsapp = normalizarWhatsapp(whatsapp);
    }

    private String normalizarWhatsapp(String valor) {
        if (valor == null || valor.isBlank()) return null;
        String numero = valor.replaceAll("\\D", "");
        if (numero.length() < 10 || numero.length() > 15) {
            throw new IllegalArgumentException("WhatsApp deve conter DDI e DDD.");
        }
        return numero;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getSlug() { return slug; }
    public boolean isAtiva() { return ativa; }
    public boolean isFinanceiroAtivo() { return financeiroAtivo; }
    public String getWhatsapp() { return whatsapp; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
    public void configurar(String nome, boolean financeiroAtivo, String whatsapp) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome da loja é obrigatório.");
        this.nome = nome.trim();
        this.financeiroAtivo = financeiroAtivo;
        this.whatsapp = normalizarWhatsapp(whatsapp);
    }
}
