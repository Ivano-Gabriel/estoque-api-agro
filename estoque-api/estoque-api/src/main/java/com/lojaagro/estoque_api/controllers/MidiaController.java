package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.services.CloudinaryAssinaturaService;
import com.lojaagro.estoque_api.services.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/midias")
public class MidiaController {
    private final CloudinaryAssinaturaService assinaturas;
    private final UsuarioService usuarios;

    public MidiaController(CloudinaryAssinaturaService assinaturas, UsuarioService usuarios) {
        this.assinaturas = assinaturas;
        this.usuarios = usuarios;
    }

    @GetMapping("/assinatura-upload")
    public CloudinaryAssinaturaService.AssinaturaUpload assinatura(Authentication auth) {
        return assinaturas.assinar(usuarios.lojaAtual(auth));
    }
}
