package com.lojaagro.estoque_api.repositories;

import com.lojaagro.estoque_api.entities.FluxoCaixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FluxoCaixaRepository extends JpaRepository<FluxoCaixa, Long> {
    // O sistema terá apenas UM registro de fluxo de caixa
}