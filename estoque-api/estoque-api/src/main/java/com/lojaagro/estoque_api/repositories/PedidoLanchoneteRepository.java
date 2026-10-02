package com.lojaagro.estoque_api.repositories;
import com.lojaagro.estoque_api.entities.*;import org.springframework.data.jpa.repository.*;import jakarta.persistence.LockModeType;import java.util.*;
public interface PedidoLanchoneteRepository extends JpaRepository<PedidoLanchonete,UUID>{
 @EntityGraph(attributePaths={"usuario","cliente","mesa","venda"})Optional<PedidoLanchonete>findByIdAndLojaId(UUID id,Long lojaId);
 @EntityGraph(attributePaths={"usuario","cliente","mesa","venda"})List<PedidoLanchonete>findTop100ByLojaIdAndStatusInOrderByCriadoEmAsc(Long lojaId,Collection<StatusPedido>status);
 @EntityGraph(attributePaths={"usuario","cliente","mesa","venda"})List<PedidoLanchonete>findTop100ByLojaIdOrderByCriadoEmDesc(Long lojaId);
 @Lock(LockModeType.PESSIMISTIC_WRITE)@Query("select p from PedidoLanchonete p where p.id=:id and p.loja.id=:lojaId")Optional<PedidoLanchonete>bloquear(UUID id,Long lojaId);
 @Query("select coalesce(max(p.numero),0) from PedidoLanchonete p where p.loja.id=:lojaId")long maiorNumero(Long lojaId);
 boolean existsByVendaId(UUID vendaId);
}
