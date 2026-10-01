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
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CloudinaryAssinaturaService {
    private static final Logger log = LoggerFactory.getLogger(CloudinaryAssinaturaService.class);
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

    public String validarUrl(String url, Loja loja) {
        if (url == null || url.isBlank()) return null;
        try {
            URI uri = URI.create(url.trim());
            String prefixo = "/" + cloudName + "/image/upload/";
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || !"res.cloudinary.com".equalsIgnoreCase(uri.getHost())
                    || !uri.getPath().startsWith(prefixo)
                    || !uri.getPath().contains("/estoque/" + loja.getSlug() + "/")) {
                throw new IllegalArgumentException("A foto deve pertencer ao armazenamento desta loja.");
            }
            return url.trim();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("A foto deve pertencer ao armazenamento desta loja.");
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void removerDepoisDoCommit(ImagemRemovidaEvento evento) {
        remover(evento.url());
    }

    private void remover(String url) {
        if (apiSecret.isBlank() || cloudName.isBlank()) return;
        try {
            String caminho = URI.create(url).getPath();
            String marcador = "/image/upload/";
            String publicId = caminho.substring(caminho.indexOf(marcador) + marcador.length())
                    .replaceFirst("^v\\d+/", "").replaceFirst("\\.[A-Za-z0-9]+$", "");
            long timestamp = Instant.now().getEpochSecond();
            String assinatura = sha1("public_id=" + publicId + "&timestamp=" + timestamp + apiSecret);
            String corpo = "public_id=" + codificar(publicId) + "&timestamp=" + timestamp
                    + "&api_key=" + codificar(apiKey) + "&signature=" + assinatura;
            HttpRequest request = HttpRequest.newBuilder(URI.create(
                            "https://api.cloudinary.com/v1_1/" + cloudName + "/image/destroy"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(corpo)).build();
            HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(resposta -> {
                        if (resposta.statusCode() >= 300) log.warn("Falha ao remover mídia antiga: HTTP {}", resposta.statusCode());
                    }).exceptionally(ex -> { log.warn("Falha ao remover mídia antiga", ex); return null; });
        } catch (Exception ex) { log.warn("URL de mídia antiga não pôde ser removida", ex); }
    }

    private String codificar(String valor) { return URLEncoder.encode(valor, StandardCharsets.UTF_8); }

    public record ImagemRemovidaEvento(String url) {}

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
