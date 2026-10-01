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
    private final com.lojaagro.estoque_api.services.AuditoriaService auditoria;

    public CategoriaController(CategoriaService service, UsuarioService usuarios,
                               com.lojaagro.estoque_api.services.AuditoriaService auditoria) {
        this.service = service;
        this.usuarios = usuarios;
        this.auditoria = auditoria;
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
        Categoria salva = service.salvar(categoria.nome(), usuarios.lojaAtual(auth));
        auditoria.registrar(usuarios.atual(auth), "CRIAR", "CATEGORIA", salva.getId(), salva.getNome());
        return ResponseEntity.ok(salva);
    }


}
