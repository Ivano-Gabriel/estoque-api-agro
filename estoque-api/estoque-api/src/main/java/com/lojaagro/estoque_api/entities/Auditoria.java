package com.lojaagro.estoque_api.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria", indexes = {
        @Index(name = "idx_auditoria_loja_data", columnList = "loja_id,criada_em"),
        @Index(name = "idx_auditoria_recurso", columnList = "loja_id,recurso,recurso_id")
})
public class Auditoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loja_id")
    private Loja loja;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    @Column(name = "usuario_email", nullable = false, length = 150)
    private String usuarioEmail;
    @Column(nullable = false, length = 60)
    private String acao;
    @Column(nullable = false, length = 60)
    private String recurso;
    @Column(name = "recurso_id", length = 80)
    private String recursoId;
    @Column(length = 2000)
    private String detalhes;
    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    protected Auditoria() {}

    public Auditoria(Loja loja, Usuario usuario, String acao, String recurso,
                     String recursoId, String detalhes, LocalDateTime criadaEm) {
        this.loja = loja;
        this.usuario = usuario;
        this.usuarioEmail = usuario == null ? "sistema" : usuario.getEmail();
        this.acao = limitar(acao, 60);
        this.recurso = limitar(recurso, 60);
        this.recursoId = limitar(recursoId, 80);
        this.detalhes = limitar(detalhes, 2000);
        this.criadaEm = criadaEm;
    }

    private String limitar(String valor, int limite) {
        if (valor == null || valor.isBlank()) return null;
        String limpo = valor.trim();
        return limpo.length() <= limite ? limpo : limpo.substring(0, limite);
    }

    public Long getId() { return id; }
    public Loja getLoja() { return loja; }
    public String getUsuarioEmail() { return usuarioEmail; }
    public String getAcao() { return acao; }
    public String getRecurso() { return recurso; }
    public String getRecursoId() { return recursoId; }
    public String getDetalhes() { return detalhes; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
}
