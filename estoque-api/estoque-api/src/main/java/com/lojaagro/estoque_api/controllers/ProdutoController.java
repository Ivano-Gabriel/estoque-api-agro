package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.entities.Produto;
import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.dto.MovimentacaoRequest;
import com.lojaagro.estoque_api.dto.ProdutoRequest;
import com.lojaagro.estoque_api.services.ProdutoService;
import com.lojaagro.estoque_api.services.UsuarioService;
import com.lojaagro.estoque_api.repositories.ProdutoRepository; // <-- Importante!
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService service;
    private final UsuarioService usuarioService;
    private final ProdutoRepository produtoRepository; // <-- Adicionado aqui!

    public ProdutoController(ProdutoService service, UsuarioService usuarioService, ProdutoRepository produtoRepository) {
        this.service = service;
        this.usuarioService = usuarioService;
        this.produtoRepository = produtoRepository; // <-- Injetado aqui!
    }

    @GetMapping
    public ResponseEntity<List<Produto>> buscarTodos() {
        return ResponseEntity.ok(service.buscarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Produto> criar(@Valid @RequestBody ProdutoRequest produto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(produto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable Long id,
                                             @Valid @RequestBody ProdutoRequest produto) {
        return ResponseEntity.ok(service.atualizar(id, produto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // NOSSAS ROTAS DA LIXEIRA
    @GetMapping("/lixeira")
    public ResponseEntity<List<Produto>> listarLixeira() {
        return ResponseEntity.ok(produtoRepository.buscarLixeira());
    }

    @PutMapping("/{id}/restaurar")
    public ResponseEntity<Void> restaurarProduto(@PathVariable Long id) {
        produtoRepository.restaurarProduto(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/venda-com-lucro")
    public ResponseEntity<Produto> vendaComLucro(
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoRequest dados,
            Authentication authentication) {
        Usuario usuario = usuarioService.buscarPorEmail(authentication.getName());
        Produto produto = service.venderComLucro(id, dados.quantidade(), dados.preco(), usuario);
        
        return ResponseEntity.ok(produto);
    }

    @PutMapping("/{id}/compra-com-custo")
    public ResponseEntity<Produto> compraComCusto(
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoRequest dados,
            Authentication authentication) {
        Usuario usuario = usuarioService.buscarPorEmail(authentication.getName());
        Produto produto = service.comprarComCusto(id, dados.quantidade(), dados.preco(), usuario);
        
        return ResponseEntity.ok(produto);
    }
}
