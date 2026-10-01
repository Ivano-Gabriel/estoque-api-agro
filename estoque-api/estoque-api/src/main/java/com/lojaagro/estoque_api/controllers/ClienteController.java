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
    private final com.lojaagro.estoque_api.services.AuditoriaService auditoria;

    public ClienteController(ClienteService clientes, UsuarioService usuarios,
                             com.lojaagro.estoque_api.services.AuditoriaService auditoria) {
        this.clientes = clientes;
        this.usuarios = usuarios;
        this.auditoria = auditoria;
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
        ClienteResponse criado=clientes.criar(request, usuarios.lojaAtual(auth));
        auditoria.registrar(usuarios.atual(auth),"CRIAR","CLIENTE",criado.id(),criado.nome()); return criado;
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(@PathVariable Long id,
                                     @Valid @RequestBody ClienteRequest request,
                                     Authentication auth) {
        ClienteResponse atualizado=clientes.atualizar(id, request, usuarios.lojaAtual(auth));
        auditoria.registrar(usuarios.atual(auth),"ATUALIZAR","CLIENTE",id,atualizado.nome()); return atualizado;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void arquivar(@PathVariable Long id, Authentication auth) {
        clientes.arquivar(id, usuarios.lojaAtual(auth));
        auditoria.registrar(usuarios.atual(auth),"ARQUIVAR","CLIENTE",id,null);
    }

    @PostMapping("/{id}/anonimizacao")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void anonimizar(@PathVariable Long id, Authentication auth) {
        clientes.anonimizar(id,usuarios.lojaAtual(auth));
        auditoria.registrar(usuarios.atual(auth),"ANONIMIZAR","CLIENTE",id,"Dados pessoais removidos; histórico comercial preservado.");
    }

    @GetMapping("/{id}/exportacao")
    @PreAuthorize("hasRole('ADMIN')")
    public ClienteResponse exportar(@PathVariable Long id, Authentication auth) {
        ClienteResponse resposta=clientes.buscar(id,usuarios.lojaAtual(auth));
        auditoria.registrar(usuarios.atual(auth),"EXPORTAR_DADOS","CLIENTE",id,null);
        return resposta;
    }
}
