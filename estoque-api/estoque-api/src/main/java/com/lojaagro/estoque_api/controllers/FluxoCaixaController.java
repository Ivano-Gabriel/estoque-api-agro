package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.entities.FluxoCaixa;
import com.lojaagro.estoque_api.services.FluxoCaixaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fluxo-caixa")
public class FluxoCaixaController {

    private final FluxoCaixaService service;

    public FluxoCaixaController(FluxoCaixaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<FluxoCaixa> getFluxoAtual() {
        return ResponseEntity.ok(service.getFluxoAtual());
    }
}
