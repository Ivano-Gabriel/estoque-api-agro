package com.lojaagro.estoque_api.repositories;

import com.lojaagro.estoque_api.entities.NotaRecebida;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotaRecebidaRepository extends JpaRepository<NotaRecebida, Long> {
    List<NotaRecebida> findByLojaIdOrderByDataRecebimentoDescIdDesc(Long lojaId);
    Optional<NotaRecebida> findByIdAndLojaId(Long id, Long lojaId);
    boolean existsByLojaIdAndChaveAcesso(Long lojaId, String chaveAcesso);
}
