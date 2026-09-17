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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.util.List;
import com.lojaagro.estoque_api.dto.ImportacaoPlanilhaResultado;
import com.lojaagro.estoque_api.services.ProdutoImportacaoService;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService service;
    private final UsuarioService usuarioService;
    private final ProdutoRepository produtoRepository; // <-- Adicionado aqui!
    private final ProdutoImportacaoService importacaoService;

    public ProdutoController(ProdutoService service,
                             UsuarioService usuarioService,
                             ProdutoRepository produtoRepository,
                             ProdutoImportacaoService importacaoService) {
        this.service = service;
        this.usuarioService = usuarioService;
        this.produtoRepository = produtoRepository; // <-- Injetado aqui!
        this.importacaoService = importacaoService;
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

    @PostMapping(value = "/importacao", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImportacaoPlanilhaResultado> importarPlanilha(
            @RequestPart("arquivo") MultipartFile arquivo) {
        ImportacaoPlanilhaResultado resultado = importacaoService.importar(arquivo);
        if (!resultado.erros().isEmpty()) {
            return ResponseEntity.unprocessableEntity().body(resultado);
        }
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/importacao/modelo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> baixarModeloImportacao() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=modelo-importacao-estoque.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(importacaoService.gerarModelo());
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
