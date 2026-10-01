package com.lojaagro.estoque_api.repositories;

import com.lojaagro.estoque_api.entities.CaixaSessao;
import com.lojaagro.estoque_api.entities.StatusCaixa;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;
import java.util.UUID;

public interface CaixaSessaoRepository extends JpaRepository<CaixaSessao, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CaixaSessao c WHERE c.loja.id=:lojaId AND c.operador.id=:operadorId AND c.status=:status")
    Optional<CaixaSessao> bloquearAberto(Long lojaId, Long operadorId, StatusCaixa status);
    Optional<CaixaSessao> findByLojaIdAndOperadorIdAndStatus(Long lojaId, Long operadorId, StatusCaixa status);
    Page<CaixaSessao> findByLojaId(Long lojaId, Pageable pageable);
}
