package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.entities.Produto;
import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.services.ProdutoService;
import com.lojaagro.estoque_api.services.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/produtos")
@CrossOrigin(origins = "*")
public class ProdutoController {

    private final ProdutoService service;
    private final UsuarioService usuarioService;

    public ProdutoController(ProdutoService service, UsuarioService usuarioService) {
        this.service = service;
        this.usuarioService = usuarioService;
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
    public ResponseEntity<Produto> salvar(@RequestBody Produto produto) {
        return ResponseEntity.ok(service.salvar(produto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/vender")
    public ResponseEntity<Produto> realizarVenda(@PathVariable Long id, @RequestParam int quantidade) {
        return ResponseEntity.ok(service.realizarVenda(id, quantidade));
    }

    @PutMapping("/{id}/comprar")
    public ResponseEntity<Produto> realizarCompra(@PathVariable Long id, @RequestParam int quantidade) {
        return ResponseEntity.ok(service.realizarCompra(id, quantidade));
    }
    
    @PutMapping("/{id}/venda-com-lucro")
    public ResponseEntity<Produto> vendaComLucro(
            @PathVariable Long id,
            @RequestBody Map<String, Object> dados) {
        
        int quantidade = ((Number) dados.get("quantidade")).intValue();
        double precoVenda = ((Number) dados.get("precoVenda")).doubleValue();
        Long usuarioId = ((Number) dados.get("usuarioId")).longValue();
        
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        Produto produto = service.venderComLucro(id, quantidade, precoVenda, usuario);
        
        return ResponseEntity.ok(produto);
    }

    @PutMapping("/{id}/compra-com-custo")
    public ResponseEntity<Produto> compraComCusto(
            @PathVariable Long id,
            @RequestBody Map<String, Object> dados) {
        
        int quantidade = ((Number) dados.get("quantidade")).intValue();
        double precoCompra = ((Number) dados.get("precoCompra")).doubleValue();
        Long usuarioId = ((Number) dados.get("usuarioId")).longValue();
        
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        Produto produto = service.comprarComCusto(id, quantidade, precoCompra, usuario);
        
        return ResponseEntity.ok(produto);
    }
}