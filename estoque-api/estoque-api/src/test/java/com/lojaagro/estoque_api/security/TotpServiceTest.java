package com.lojaagro.estoque_api.security;

import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class TotpServiceTest {
    @Test void validaVetorRfc6238EmSeisDigitos() {
        TotpService totp = new TotpService("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ",
                Clock.fixed(Instant.ofEpochSecond(59), ZoneOffset.UTC));
        assertTrue(totp.configurado());
        assertTrue(totp.validar("287082"));
        assertFalse(totp.validar("287083"));
        assertFalse(totp.validar("abc"));
    }

    @Test void segredoAusenteNuncaValida() {
        TotpService totp = new TotpService("", Clock.systemUTC());
        assertFalse(totp.configurado());
        assertFalse(totp.validar("000000"));
    }
}
