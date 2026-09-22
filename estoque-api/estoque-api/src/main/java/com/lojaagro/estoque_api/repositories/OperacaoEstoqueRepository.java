package com.lojaagro.estoque_api.repositories;

import com.lojaagro.estoque_api.entities.OperacaoEstoque;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperacaoEstoqueRepository extends JpaRepository<OperacaoEstoque, UUID> {
    java.util.Optional<OperacaoEstoque> findByIdAndLojaId(UUID id, Long lojaId);
}
