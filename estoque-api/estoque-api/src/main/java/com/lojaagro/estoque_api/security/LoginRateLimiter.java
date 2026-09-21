package com.lojaagro.estoque_api.security;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

/** Proteção local e limitada em memória; não confia em X-Forwarded-For enviado pelo cliente. */
@Component
public class LoginRateLimiter {
    private final Clock clock;
    private final Map<String, Janela> contas = new HashMap<>();
    private Janela global;

    public LoginRateLimiter(Clock clock) { this.clock = clock; }

    public synchronized void verificar(String email) {
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

    private static class Janela {
        final long inicio;
        int tentativas;
        Janela(long inicio) { this.inicio = inicio; }
    }
}
