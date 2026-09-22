package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.dto.RelatorioWhatsappResponse;
import com.lojaagro.estoque_api.services.RelatorioWhatsappService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import com.lojaagro.estoque_api.services.UsuarioService;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    private final RelatorioWhatsappService service;
    private final UsuarioService usuarios;

    public RelatorioController(RelatorioWhatsappService service, UsuarioService usuarios) {
        this.service = service; this.usuarios = usuarios;
    }

    @GetMapping("/whatsapp")
    public ResponseEntity<RelatorioWhatsappResponse> gerarWhatsapp(
            @RequestParam String periodo, Authentication auth) {
        return ResponseEntity.ok(service.gerar(periodo, usuarios.lojaAtual(auth)));
    }
}
