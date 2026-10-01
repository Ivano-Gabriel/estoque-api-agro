package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.dto.CancelarVendaRequest;
import com.lojaagro.estoque_api.dto.VendaRequest;
import com.lojaagro.estoque_api.dto.VendaResponse;
import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.services.UsuarioService;
import com.lojaagro.estoque_api.services.VendaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vendas")
public class VendaController {
    private final VendaService vendas;
    private final UsuarioService usuarios;

    public VendaController(VendaService vendas, UsuarioService usuarios) {
        this.vendas = vendas;
        this.usuarios = usuarios;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendaResponse concluir(@RequestHeader(value = "Idempotency-Key", required = false) String chave,
                                  @Valid @RequestBody VendaRequest request, Authentication auth) {
        if (chave == null || chave.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key é obrigatório.");
        }
        UUID id;
        try { id = UUID.fromString(chave); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException("Idempotency-Key inválido."); }
        return vendas.concluir(id, request, usuarios.atual(auth));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<VendaResponse> listar(Authentication auth) {
        return vendas.listar(usuarios.lojaAtual(auth));
    }

    @GetMapping("/pagina")
    @PreAuthorize("hasRole('ADMIN')")
    public com.lojaagro.estoque_api.dto.PaginaResponse<VendaResponse> pesquisar(
            @RequestParam(defaultValue="0") int pagina,
            @RequestParam(defaultValue="25") int tamanho,
            @RequestParam(required=false) java.time.LocalDateTime inicio,
            @RequestParam(required=false) java.time.LocalDateTime fim,
            @RequestParam(required=false) com.lojaagro.estoque_api.entities.StatusVenda status,
            @RequestParam(defaultValue="") String busca,
            Authentication auth) {
        return vendas.pesquisar(usuarios.lojaAtual(auth),pagina,tamanho,inicio,fim,status,busca);
    }

    @PutMapping("/{id}/cancelamento")
    @PreAuthorize("hasRole('ADMIN')")
    public VendaResponse cancelar(@PathVariable UUID id,
                                  @Valid @RequestBody CancelarVendaRequest request,
                                  Authentication auth) {
        Usuario responsavel = usuarios.atual(auth);
        return vendas.cancelar(id, request.motivo(), responsavel);
    }

    @PostMapping("/{id}/devolucoes")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public com.lojaagro.estoque_api.dto.DevolucaoVendaResponse devolver(
            @PathVariable UUID id,
            @Valid @RequestBody com.lojaagro.estoque_api.dto.DevolucaoVendaRequest request,
            Authentication auth) {
        return vendas.devolver(id, request, usuarios.atual(auth));
    }

    @GetMapping("/{id}/devolucoes")
    @PreAuthorize("hasRole('ADMIN')")
    public java.util.List<com.lojaagro.estoque_api.dto.DevolucaoVendaResponse> devolucoes(
            @PathVariable UUID id, Authentication auth) {
        return vendas.listarDevolucoes(id, usuarios.lojaAtual(auth));
    }
}
