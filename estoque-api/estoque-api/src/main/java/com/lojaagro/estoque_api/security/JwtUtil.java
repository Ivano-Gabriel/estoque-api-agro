package com.lojaagro.estoque_api.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final String segredo;
    private final long expiracao;

    public JwtUtil(@Value("${app.jwt.secret}") String segredo,
                   @Value("${app.jwt.expiration-ms}") long expiracao) {
        this.segredo = segredo;
        this.expiracao = expiracao;
    }

    @PostConstruct
    void validarConfiguracao() {
        if (segredo == null || segredo.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET deve ter pelo menos 32 bytes");
        }
    }

    private SecretKey getChave() {
        return Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
    }

    public String gerarToken(com.lojaagro.estoque_api.entities.Usuario usuario) {
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("sv", usuario.getVersaoSessao())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiracao))
                .signWith(getChave())
                .compact();
    }

    public String extrairEmail(String token) {
        return Jwts.parser()
                .verifyWith(getChave())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validarToken(String token) {
        try {
            extrairEmail(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean sessaoAtual(String token, com.lojaagro.estoque_api.entities.Usuario usuario) {
        try {
            Integer versao = Jwts.parser().verifyWith(getChave()).build()
                    .parseSignedClaims(token).getPayload().get("sv", Integer.class);
            return usuario.isAtivo() && versao != null && versao == usuario.getVersaoSessao();
        } catch (Exception e) { return false; }
    }
}
