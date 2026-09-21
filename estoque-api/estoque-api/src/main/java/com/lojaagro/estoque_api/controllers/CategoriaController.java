package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.entities.Categoria;
import com.lojaagro.estoque_api.services.CategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Categoria>> buscarTodos() {
        return ResponseEntity.ok(service.buscarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Categoria> salvar(@jakarta.validation.Valid @RequestBody com.lojaagro.estoque_api.dto.CategoriaRequest categoria) {
        return ResponseEntity.ok(service.salvar(new Categoria(categoria.nome().trim())));
    }


}
