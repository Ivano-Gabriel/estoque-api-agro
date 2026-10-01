package com.lojaagro.estoque_api.repositories;

import com.lojaagro.estoque_api.entities.MovimentoCaixa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MovimentoCaixaRepository extends JpaRepository<MovimentoCaixa, UUID> {
    List<MovimentoCaixa> findBySessaoIdOrderByCriadoEmAsc(UUID sessaoId);
}
