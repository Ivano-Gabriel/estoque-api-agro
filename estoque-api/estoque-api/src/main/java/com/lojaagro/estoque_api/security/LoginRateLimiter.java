package com.lojaagro.estoque_api.security;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.lojaagro.estoque_api.entities.LimiteLogin;
import com.lojaagro.estoque_api.repositories.LimiteLoginRepository;

/** Proteção compartilhada pelo banco; não confia em IP informado pelo cliente. */
@Component
public class LoginRateLimiter {
    private final Clock clock;
    private final LimiteLoginRepository repository;
    private final Map<String, Janela> contas = new HashMap<>();
    private Janela global;

    @Autowired
    public LoginRateLimiter(Clock clock, LimiteLoginRepository repository) {
        this.clock = clock;
        this.repository = repository;
    }

    /** Construtor preservado para o teste unitário sem banco. */
    LoginRateLimiter(Clock clock) { this.clock = clock; this.repository = null; }

    @Transactional
    public void verificar(String email) {
        if (repository != null) {
            verificarDistribuido(email);
            return;
        }
        verificarLocal(email);
    }

    private synchronized void verificarLocal(String email) {
        long agora = clock.millis();
        contas.entrySet().removeIf(e -> agora - e.getValue().inicio >= 300_000);
        if (global == null || agora - global.inicio >= 60_000) global = new Janela(agora);
        Janela conta = contas.get(email);
        if (global.tentativas >= 60 || (conta != null && conta.tentativas >= 10) || contas.size() >= 10_000) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas tentativas de login. Aguarde alguns minutos e tente novamente.");
        }
        global.tentativas++;
        contas.computeIfAbsent(email, chave -> new Janela(agora)).tentativas++;
    }

    private void verificarDistribuido(String email) {
        Instant agora = clock.instant();
        LimiteLogin globalPersistido = repository.bloquear("global")
                .orElseGet(() -> repository.saveAndFlush(new LimiteLogin("global", agora)));
        if (globalPersistido.getInicio().plusSeconds(60).isBefore(agora)) {
            repository.deleteByInicioBeforeAndIdNot(agora.minusSeconds(86_400), "global");
        }
        String contaId = "conta:" + hash(email);
        LimiteLogin conta = repository.bloquear(contaId)
                .orElseGet(() -> repository.saveAndFlush(new LimiteLogin(contaId, agora)));
        if (globalPersistido.bloqueado(agora, 60, 60) || conta.bloqueado(agora, 300, 10)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas tentativas de login. Aguarde alguns minutos e tente novamente.");
        }
        globalPersistido.registrar(agora, 60);
        conta.registrar(agora, 300);
    }

    private String hash(String valor) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponível.", ex);
        }
    }

    private static class Janela {
        final long inicio;
        int tentativas;
        Janela(long inicio) { this.inicio = inicio; }
    }
}
