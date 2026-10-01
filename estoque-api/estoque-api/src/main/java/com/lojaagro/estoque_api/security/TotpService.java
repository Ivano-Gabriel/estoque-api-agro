package com.lojaagro.estoque_api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Clock;

/** Verificação RFC 6238, janela de 30 segundos e tolerância de um intervalo. */
@Component
public class TotpService {
    private final byte[] segredo;
    private final Clock clock;

    public TotpService(@Value("${app.platform-admin.mfa-secret:}") String segredoBase32, Clock clock) {
        this.segredo = segredoBase32 == null || segredoBase32.isBlank()
                ? new byte[0] : decodificarBase32(segredoBase32);
        this.clock = clock;
    }

    public boolean configurado() { return segredo.length >= 20; }

    public boolean validar(String codigo) {
        if (!configurado() || codigo == null || !codigo.matches("\\d{6}")) return false;
        long contador = clock.instant().getEpochSecond() / 30;
        for (long deslocamento = -1; deslocamento <= 1; deslocamento++) {
            String esperado = gerar(contador + deslocamento);
            if (java.security.MessageDigest.isEqual(esperado.getBytes(StandardCharsets.US_ASCII),
                    codigo.getBytes(StandardCharsets.US_ASCII))) return true;
        }
        return false;
    }

    private String gerar(long contador) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(segredo, "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(contador).array());
            int offset = hash[hash.length - 1] & 0x0f;
            int binario = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);
            return String.format("%06d", binario % 1_000_000);
        } catch (Exception ex) {
            throw new IllegalStateException("Não foi possível validar o segundo fator.", ex);
        }
    }

    private byte[] decodificarBase32(String valor) {
        String alfabeto = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        String normalizado = valor.replace("=", "").replaceAll("\\s", "").toUpperCase();
        java.io.ByteArrayOutputStream saida = new java.io.ByteArrayOutputStream();
        int buffer = 0, bits = 0;
        for (char caractere : normalizado.toCharArray()) {
            int indice = alfabeto.indexOf(caractere);
            if (indice < 0) throw new IllegalStateException("PLATFORM_ADMIN_MFA_SECRET não está em Base32.");
            buffer = (buffer << 5) | indice;
            bits += 5;
            if (bits >= 8) {
                saida.write((buffer >> (bits - 8)) & 0xff);
                bits -= 8;
            }
        }
        return saida.toByteArray();
    }
}
