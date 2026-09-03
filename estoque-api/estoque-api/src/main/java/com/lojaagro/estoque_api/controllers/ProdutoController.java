package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.entities.Produto;
import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.services.ProdutoService;
import com.lojaagro.estoque_api.services.UsuarioService;
import com.lojaagro.estoque_api.repositories.ProdutoRepository; // <-- Importante!
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/produtos")
@CrossOrigin(origins = "*")
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

    @Autowired
    private JdbcTemplate jdbcTemplate;
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
    @DeleteMapping("/{id}/permanente")
    public ResponseEntity<Void> deletarPermanente(@PathVariable Long id) {
        // Apaga as transações primeiro (libera o banco)
        produtoRepository.apagarTransacoesDoProduto(id);
        // Agora pode aniquilar o produto de vez
        produtoRepository.apagarPermanente(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reset-financeiro")
    public ResponseEntity<Void> resetarFinanceiro() {
        System.out.println("🚨 RECEBI O PEDIDO DE RESET DO FRONT-END!");
        try {
            // Apaga todo o histórico de compras e vendas
            jdbcTemplate.execute("DELETE FROM transacao");
            
            // Zera o caixa (e se por acaso estiver vazio, já insere um zerado)
            jdbcTemplate.execute("DELETE FROM fluxo_caixa");
            jdbcTemplate.execute("INSERT INTO fluxo_caixa (id, total_entradas, total_saidas, saldo_liquido) VALUES (1, 0, 0, 0)");
            
            System.out.println("✅ BANCO DE DADOS FINANCEIRO ZERADO COM SUCESSO!");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println("❌ DEU ERRO AO TENTAR ZERAR O BANCO:");
            e.printStackTrace(); 
            return ResponseEntity.internalServerError().build();
        }
    }
}