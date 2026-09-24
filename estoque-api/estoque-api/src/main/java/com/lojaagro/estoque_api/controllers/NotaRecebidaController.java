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
    public record Conferencia(@NotNull Boolean conferida) {}

    public NotaRecebidaController(NotaRecebidaService notas, UsuarioService usuarios) {
        this.notas = notas; this.usuarios = usuarios;
    }
    @GetMapping public List<NotaRecebidaResponse> listar(Authentication auth) {
        return notas.listar(usuarios.lojaAtual(auth));
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public NotaRecebidaResponse criar(@Valid @RequestBody NotaRecebidaRequest request, Authentication auth) {
        return notas.criar(request, usuarios.atual(auth));
    }
    @PutMapping("/{id}/conferencia")
    public NotaRecebidaResponse conferir(@PathVariable Long id, @Valid @RequestBody Conferencia request,
                                         Authentication auth) {
        return notas.alterarConferencia(id, request.conferida(), usuarios.lojaAtual(auth));
    }
}
