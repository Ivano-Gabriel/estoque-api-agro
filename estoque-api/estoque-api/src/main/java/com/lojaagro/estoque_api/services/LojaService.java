package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.controllers.AuthController;
import com.lojaagro.estoque_api.dto.CriarLojaRequest;
import com.lojaagro.estoque_api.entities.FluxoCaixa;
import com.lojaagro.estoque_api.entities.Loja;
import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.entities.UsuarioRole;
import com.lojaagro.estoque_api.repositories.FluxoCaixaRepository;
import com.lojaagro.estoque_api.repositories.LojaRepository;
import com.lojaagro.estoque_api.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class LojaService {
    private final LojaRepository lojas;
    private final UsuarioRepository usuarios;
    private final FluxoCaixaRepository caixas;
    private final PasswordEncoder encoder;

    public LojaService(LojaRepository lojas, UsuarioRepository usuarios,
                       FluxoCaixaRepository caixas, PasswordEncoder encoder) {
        this.lojas = lojas; this.usuarios = usuarios; this.caixas = caixas; this.encoder = encoder;
    }

    public List<Loja> listar() { return lojas.findAll(); }

    @Transactional
    public Loja criar(CriarLojaRequest request) {
        String email = request.adminEmail().trim().toLowerCase(java.util.Locale.ROOT);
        if (lojas.existsBySlug(request.slug())) throw new IllegalArgumentException("Identificador da loja já utilizado.");
        if (usuarios.existsByEmail(email)) throw new IllegalArgumentException("E-mail do administrador já utilizado.");
        AuthController.validarSenha(request.adminSenha());
        Loja loja = lojas.saveAndFlush(new Loja(request.nome(), request.slug(), request.financeiroAtivo(), request.whatsapp()));
        caixas.save(new FluxoCaixa(loja.getId(), loja));
        Usuario admin = new Usuario();
        admin.setEmail(email);
        admin.setSenha(encoder.encode(request.adminSenha()));
        admin.setRole(UsuarioRole.ADMIN);
        admin.setLoja(loja);
        usuarios.save(admin);
        return loja;
    }

    @Transactional
    public Loja alterarStatus(Long id, boolean ativa) {
        Loja loja = lojas.findById(id).orElseThrow(() -> new IllegalArgumentException("Loja não encontrada."));
        loja.setAtiva(ativa);
        if (!ativa) usuarios.findByLojaId(id).forEach(Usuario::revogarSessoes);
        return loja;
    }

    @Transactional
    public Loja configurar(Long id, String nome, boolean financeiroAtivo, String whatsapp) {
        Loja loja = lojas.findById(id).orElseThrow(() -> new IllegalArgumentException("Loja não encontrada."));
        loja.configurar(nome, financeiroAtivo, whatsapp);
        usuarios.findByLojaId(id).forEach(Usuario::revogarSessoes);
        return loja;
    }
}
