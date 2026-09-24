package com.lojaagro.estoque_api.repositories;

import com.lojaagro.estoque_api.entities.Venda;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendaRepository extends JpaRepository<Venda, UUID> {
    @EntityGraph(attributePaths = {"itens", "cliente", "usuario"})
    List<Venda> findTop50ByLojaIdOrderByCriadaEmDesc(Long lojaId);

    @EntityGraph(attributePaths = {"itens", "cliente", "usuario"})
    Optional<Venda> findByIdAndLojaId(UUID id, Long lojaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT DISTINCT v FROM Venda v LEFT JOIN FETCH v.itens WHERE v.id = :id AND v.loja.id = :lojaId")
    Optional<Venda> bloquearPorIdELoja(UUID id, Long lojaId);
}
