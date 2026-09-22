package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.entities.Categoria;
import com.lojaagro.estoque_api.services.CategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.lojaagro.estoque_api.services.UsuarioService;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService service;
    private final UsuarioService usuarios;

    public CategoriaController(CategoriaService service, UsuarioService usuarios) {
        this.service = service;
        this.usuarios = usuarios;
    }

    @GetMapping
    public ResponseEntity<List<Categoria>> buscarTodos(Authentication auth) {
        return ResponseEntity.ok(service.buscarTodos(usuarios.lojaAtual(auth)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> buscarPorId(@PathVariable Long id, Authentication auth) {
        return service.buscarPorId(id, usuarios.lojaAtual(auth))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Categoria> salvar(@jakarta.validation.Valid @RequestBody com.lojaagro.estoque_api.dto.CategoriaRequest categoria,
                                            Authentication auth) {
        return ResponseEntity.ok(service.salvar(categoria.nome(), usuarios.lojaAtual(auth)));
    }


}
