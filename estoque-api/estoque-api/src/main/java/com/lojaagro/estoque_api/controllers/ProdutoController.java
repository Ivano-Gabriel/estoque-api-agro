package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.entities.Produto;
import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.dto.MovimentacaoRequest;
import com.lojaagro.estoque_api.dto.ProdutoRequest;
import com.lojaagro.estoque_api.dto.CadastroProdutosLoteRequest;
import com.lojaagro.estoque_api.dto.ComprovanteVenda;
import com.lojaagro.estoque_api.services.ProdutoService;
import com.lojaagro.estoque_api.services.UsuarioService;
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
import com.lojaagro.estoque_api.services.ProdutoCadastroLoteService;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService service;
    private final UsuarioService usuarioService;
    private final ProdutoImportacaoService importacaoService;
    private final ProdutoCadastroLoteService cadastroLoteService;
    private final com.lojaagro.estoque_api.services.MovimentacaoService movimentacaoService;

    public ProdutoController(ProdutoService service,
                             UsuarioService usuarioService,
                             ProdutoImportacaoService importacaoService,
                             ProdutoCadastroLoteService cadastroLoteService,
                             com.lojaagro.estoque_api.services.MovimentacaoService movimentacaoService) {
        this.service = service;
        this.usuarioService = usuarioService;
        this.importacaoService = importacaoService;
        this.cadastroLoteService = cadastroLoteService;
        this.movimentacaoService = movimentacaoService;
    }

    @GetMapping
    public ResponseEntity<List<Produto>> buscarTodos(Authentication auth) {
        return ResponseEntity.ok(service.buscarTodos(usuarioService.lojaAtual(auth)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable Long id, Authentication auth) {
        return service.buscarPorId(id, usuarioService.lojaAtual(auth))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Produto> criar(@Valid @RequestBody ProdutoRequest produto, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(produto, usuarioService.lojaAtual(auth)));
    }

    @PostMapping("/cadastro-em-massa")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImportacaoPlanilhaResultado> cadastrarEmMassa(
            @RequestBody CadastroProdutosLoteRequest lote, Authentication auth) {
        ImportacaoPlanilhaResultado resultado = cadastroLoteService.cadastrar(
                lote, usuarioService.lojaAtual(auth));
        if (!resultado.erros().isEmpty()) {
            return ResponseEntity.unprocessableEntity().body(resultado);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable Long id,
                                             @Valid @RequestBody ProdutoRequest produto, Authentication auth) {
        return ResponseEntity.ok(service.atualizar(id, produto, usuarioService.lojaAtual(auth)));
    }

    @PostMapping(value = "/importacao", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImportacaoPlanilhaResultado> importarPlanilha(
            @RequestPart("arquivo") MultipartFile arquivo, Authentication auth) {
        ImportacaoPlanilhaResultado resultado = importacaoService.importar(arquivo, usuarioService.lojaAtual(auth));
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
    public ResponseEntity<Void> deletar(@PathVariable Long id, Authentication auth) {
        service.deletar(id, usuarioService.lojaAtual(auth));
        return ResponseEntity.noContent().build();
    }

    // NOSSAS ROTAS DA LIXEIRA
    @GetMapping("/lixeira")
    public ResponseEntity<List<Produto>> listarLixeira(Authentication auth) {
        return ResponseEntity.ok(service.buscarLixeira(usuarioService.lojaAtual(auth)));
    }

    @PutMapping("/{id}/restaurar")
    public ResponseEntity<Void> restaurarProduto(@PathVariable Long id, Authentication auth) {
        service.restaurar(id, usuarioService.lojaAtual(auth));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/venda-com-lucro")
    public ResponseEntity<?> vendaComLucro(
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoRequest dados,
            Authentication authentication,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave) {
        Usuario usuario = usuarioService.buscarPorEmail(authentication.getName());
        var transacao = movimentacaoService.executar(chave, true, id, dados, usuario);
        return transacao == null
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(ComprovanteVenda.from(transacao));
    }

    @PutMapping("/{id}/compra-com-custo")
    public ResponseEntity<Void> compraComCusto(
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoRequest dados,
            Authentication authentication,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave) {
        Usuario usuario = usuarioService.buscarPorEmail(authentication.getName());
        movimentacaoService.executar(chave, false, id, dados, usuario);
        return ResponseEntity.noContent().build();
    }
}
