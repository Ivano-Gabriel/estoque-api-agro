package com.lojaagro.estoque_api.repositories;

import com.lojaagro.estoque_api.entities.FluxoCaixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FluxoCaixaRepository extends JpaRepository<FluxoCaixa, Long> {
    java.util.Optional<FluxoCaixa> findByLojaId(Long lojaId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT f FROM FluxoCaixa f WHERE f.loja.id = :lojaId")
    java.util.Optional<FluxoCaixa> bloquearCaixa(Long lojaId);
}
