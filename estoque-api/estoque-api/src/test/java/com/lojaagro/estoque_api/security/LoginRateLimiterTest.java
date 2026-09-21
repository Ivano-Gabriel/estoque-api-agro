package com.lojaagro.estoque_api.security;

import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimiterTest {
    @Test void bloqueiaContaELiberaDepoisDaJanela() {
        AtomicLong millis = new AtomicLong(0);
        Clock clock = new Clock() {
            public ZoneId getZone() { return ZoneOffset.UTC; }
            public Clock withZone(ZoneId zone) { return this; }
            public Instant instant() { return Instant.ofEpochMilli(millis.get()); }
        };
        var limiter = new LoginRateLimiter(clock);
        for (int i = 0; i < 10; i++) limiter.verificar("teste@loja.com");
        assertEquals(429, assertThrows(ResponseStatusException.class, () -> limiter.verificar("teste@loja.com")).getStatusCode().value());
        assertDoesNotThrow(() -> limiter.verificar("outra@loja.com"));
        millis.set(300_001);
        assertDoesNotThrow(() -> limiter.verificar("teste@loja.com"));
    }

    @Test void emailsDiferentesNaoContornamLimiteGlobal() {
        var limiter = new LoginRateLimiter(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        for (int i = 0; i < 60; i++) limiter.verificar(i + "@loja.com");
        assertThrows(ResponseStatusException.class, () -> limiter.verificar("novo@loja.com"));
    }
}
