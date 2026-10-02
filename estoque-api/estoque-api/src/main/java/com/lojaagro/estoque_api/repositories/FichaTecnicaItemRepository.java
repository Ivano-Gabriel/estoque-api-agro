package com.lojaagro.estoque_api.repositories;

import com.lojaagro.estoque_api.entities.FichaTecnicaItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FichaTecnicaItemRepository extends JpaRepository<FichaTecnicaItem, Long> {
    boolean existsByIngredienteId(Long ingredienteId);
}
