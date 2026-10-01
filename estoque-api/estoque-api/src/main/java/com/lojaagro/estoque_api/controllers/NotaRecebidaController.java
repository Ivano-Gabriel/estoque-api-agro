package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.dto.NotaRecebidaRequest;
import com.lojaagro.estoque_api.dto.NotaRecebidaResponse;
import com.lojaagro.estoque_api.services.NotaRecebidaService;
import com.lojaagro.estoque_api.services.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/notas-recebidas")
@PreAuthorize("hasRole('ADMIN')")
public class NotaRecebidaController {
    private final NotaRecebidaService notas;
    private final UsuarioService usuarios;
    private final com.lojaagro.estoque_api.services.AuditoriaService auditoria;
    public record Conferencia(@NotNull Boolean conferida) {}

    public NotaRecebidaController(NotaRecebidaService notas, UsuarioService usuarios,
                                  com.lojaagro.estoque_api.services.AuditoriaService auditoria) {
        this.notas = notas; this.usuarios = usuarios; this.auditoria = auditoria;
    }
    @GetMapping public List<NotaRecebidaResponse> listar(Authentication auth) {
        return notas.listar(usuarios.lojaAtual(auth));
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public NotaRecebidaResponse criar(@Valid @RequestBody NotaRecebidaRequest request, Authentication auth) {
        NotaRecebidaResponse criada = notas.criar(request, usuarios.atual(auth));
        auditoria.registrar(usuarios.atual(auth), "REGISTRAR", "NOTA_RECEBIDA", criada.id(),
                "Fornecedor: " + criada.fornecedor() + " • Número: " + criada.numero());
        return criada;
    }
    @PutMapping("/{id}/conferencia")
    public NotaRecebidaResponse conferir(@PathVariable Long id, @Valid @RequestBody Conferencia request,
                                         Authentication auth) {
        NotaRecebidaResponse resposta = notas.alterarConferencia(id, request.conferida(), usuarios.lojaAtual(auth));
        auditoria.registrar(usuarios.atual(auth), request.conferida() ? "CONFERIR" : "REABRIR_CONFERENCIA",
                "NOTA_RECEBIDA", id, null);
        return resposta;
    }
}
