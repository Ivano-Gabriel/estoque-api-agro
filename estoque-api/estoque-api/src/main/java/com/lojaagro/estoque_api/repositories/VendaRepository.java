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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import com.lojaagro.estoque_api.entities.StatusVenda;

public interface VendaRepository extends JpaRepository<Venda, UUID> {
    @EntityGraph(attributePaths = {"itens", "pagamentos", "cliente", "usuario"})
    List<Venda> findTop50ByLojaIdOrderByCriadaEmDesc(Long lojaId);

    @EntityGraph(attributePaths = {"itens", "pagamentos", "cliente", "usuario"})
    Optional<Venda> findByIdAndLojaId(UUID id, Long lojaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT DISTINCT v FROM Venda v LEFT JOIN FETCH v.itens LEFT JOIN FETCH v.pagamentos WHERE v.id = :id AND v.loja.id = :lojaId")
    Optional<Venda> bloquearPorIdELoja(UUID id, Long lojaId);

    @Query("SELECT v.id FROM Venda v LEFT JOIN v.cliente c WHERE v.loja.id=:lojaId "
            + "AND (:inicio IS NULL OR v.criadaEm>=:inicio) AND (:fim IS NULL OR v.criadaEm<=:fim) "
            + "AND (:status IS NULL OR v.status=:status) "
            + "AND (:busca='' OR LOWER(COALESCE(c.nome,'')) LIKE LOWER(CONCAT('%',:busca,'%')) "
            + "OR LOWER(CAST(v.id AS string)) LIKE LOWER(CONCAT('%',:busca,'%')))")
    Page<UUID> buscarIds(Long lojaId, LocalDateTime inicio, LocalDateTime fim,
                         StatusVenda status, String busca, Pageable pageable);

    @Query("SELECT DISTINCT v FROM Venda v LEFT JOIN FETCH v.itens LEFT JOIN FETCH v.pagamentos "
            + "LEFT JOIN FETCH v.cliente LEFT JOIN FETCH v.usuario WHERE v.id IN :ids")
    List<Venda> buscarDetalhes(List<UUID> ids);
}
