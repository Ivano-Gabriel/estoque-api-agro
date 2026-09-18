package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.dto.RelatorioWhatsappResponse;
import com.lojaagro.estoque_api.services.RelatorioWhatsappService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    private final RelatorioWhatsappService service;

    public RelatorioController(RelatorioWhatsappService service) {
        this.service = service;
    }

    @GetMapping("/whatsapp")
    public ResponseEntity<RelatorioWhatsappResponse> gerarWhatsapp(
            @RequestParam String periodo) {
        return ResponseEntity.ok(service.gerar(periodo));
    }
}
