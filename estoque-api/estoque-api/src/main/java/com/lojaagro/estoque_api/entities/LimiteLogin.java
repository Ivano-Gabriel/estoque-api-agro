package com.lojaagro.estoque_api.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "limite_login")
public class LimiteLogin {
    @Id @Column(length = 80)
    private String id;
    @Column(nullable = false)
    private Instant inicio;
    @Column(nullable = false)
    private int tentativas;
    @Version
    private long versao;

    protected LimiteLogin() {}
    public LimiteLogin(String id, Instant inicio) { this.id = id; this.inicio = inicio; }
    public void registrar(Instant agora, long janelaSegundos) {
        if (inicio == null || inicio.plusSeconds(janelaSegundos).isBefore(agora)) {
            inicio = agora;
            tentativas = 0;
        }
        tentativas++;
    }
    public boolean bloqueado(Instant agora, long janelaSegundos, int limite) {
        return inicio != null && !inicio.plusSeconds(janelaSegundos).isBefore(agora) && tentativas >= limite;
    }
    public String getId() { return id; }
    public Instant getInicio() { return inicio; }
    public int getTentativas() { return tentativas; }
}
