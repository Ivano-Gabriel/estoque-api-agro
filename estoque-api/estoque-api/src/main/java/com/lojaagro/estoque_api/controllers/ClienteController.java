package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.dto.ClienteRequest;
import com.lojaagro.estoque_api.dto.ClienteResponse;
import com.lojaagro.estoque_api.services.ClienteService;
import com.lojaagro.estoque_api.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService clientes;
    private final UsuarioService usuarios;

    public ClienteController(ClienteService clientes, UsuarioService usuarios) {
        this.clientes = clientes;
        this.usuarios = usuarios;
    }

    @GetMapping
    public List<ClienteResponse> listar(Authentication auth) {
        return clientes.listar(usuarios.lojaAtual(auth));
    }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable Long id, Authentication auth) {
        return clientes.buscar(id, usuarios.lojaAtual(auth));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse criar(@Valid @RequestBody ClienteRequest request, Authentication auth) {
        return clientes.criar(request, usuarios.lojaAtual(auth));
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(@PathVariable Long id,
                                     @Valid @RequestBody ClienteRequest request,
                                     Authentication auth) {
        return clientes.atualizar(id, request, usuarios.lojaAtual(auth));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void arquivar(@PathVariable Long id, Authentication auth) {
        clientes.arquivar(id, usuarios.lojaAtual(auth));
    }
}
