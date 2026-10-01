package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.dto.AuditoriaResponse;
import com.lojaagro.estoque_api.entities.Auditoria;
import com.lojaagro.estoque_api.entities.Loja;
import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.repositories.AuditoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class AuditoriaService {
    private final AuditoriaRepository repository;
    private final Clock clock;

    public AuditoriaService(AuditoriaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public void registrar(Usuario usuario, String acao, String recurso, Object recursoId, String detalhes) {
        Loja loja = usuario == null ? null : usuario.getLoja();
        repository.save(new Auditoria(loja, usuario, acao, recurso,
                recursoId == null ? null : String.valueOf(recursoId), detalhes, LocalDateTime.now(clock)));
    }

    public void registrarPlataforma(Usuario usuario, Loja loja, String acao, String detalhes) {
        repository.save(new Auditoria(loja, usuario, acao, "LOJA",
                loja == null ? null : String.valueOf(loja.getId()), detalhes, LocalDateTime.now(clock)));
    }

    public Page<AuditoriaResponse> listar(Loja loja, int pagina, int tamanho) {
        int limite = Math.min(Math.max(tamanho, 1), 100);
        return repository.findByLojaId(loja.getId(), PageRequest.of(Math.max(pagina, 0), limite,
                        Sort.by(Sort.Direction.DESC, "criadaEm")))
                .map(AuditoriaResponse::de);
    }
}
