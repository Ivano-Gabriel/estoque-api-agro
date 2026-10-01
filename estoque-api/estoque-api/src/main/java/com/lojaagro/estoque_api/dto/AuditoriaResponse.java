package com.lojaagro.estoque_api.dto;

import com.lojaagro.estoque_api.entities.Auditoria;
import java.time.LocalDateTime;

public record AuditoriaResponse(Long id, String usuario, String acao, String recurso,
                                String recursoId, String detalhes, LocalDateTime criadaEm) {
    public static AuditoriaResponse de(Auditoria item) {
        return new AuditoriaResponse(item.getId(), item.getUsuarioEmail(), item.getAcao(),
                item.getRecurso(), item.getRecursoId(), item.getDetalhes(), item.getCriadaEm());
    }
}
