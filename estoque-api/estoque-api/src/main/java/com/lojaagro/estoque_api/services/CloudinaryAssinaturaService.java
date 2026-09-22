package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.entities.Loja;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class CloudinaryAssinaturaService {
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final String uploadPreset;

    public CloudinaryAssinaturaService(
            @Value("${app.cloudinary.cloud-name:}") String cloudName,
            @Value("${app.cloudinary.api-key:}") String apiKey,
            @Value("${app.cloudinary.api-secret:}") String apiSecret,
            @Value("${app.cloudinary.upload-preset:}") String uploadPreset) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.uploadPreset = uploadPreset;
    }

    public AssinaturaUpload assinar(Loja loja) {
        if (!loja.isFotosAtivas()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "O módulo de fotos não está ativo para esta loja.");
        }
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank() || uploadPreset.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Armazenamento de fotos ainda não configurado.");
        }
        long timestamp = Instant.now().getEpochSecond();
        String pasta = "estoque/" + loja.getSlug();
        String parametros = "folder=" + pasta + "&timestamp=" + timestamp + "&upload_preset=" + uploadPreset;
        return new AssinaturaUpload(cloudName, apiKey, uploadPreset, pasta, timestamp, sha1(parametros + apiSecret));
    }

    private String sha1(String valor) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1")
                    .digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Algoritmo de assinatura indisponível.", ex);
        }
    }

    public record AssinaturaUpload(
            String cloudName, String apiKey, String uploadPreset, String pasta, long timestamp, String assinatura) {}
}
