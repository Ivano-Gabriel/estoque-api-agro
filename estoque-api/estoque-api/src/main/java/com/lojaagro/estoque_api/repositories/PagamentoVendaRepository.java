package com.lojaagro.estoque_api.repositories;
import com.lojaagro.estoque_api.entities.PagamentoVenda;
import org.springframework.data.jpa.repository.*;
import java.util.List;
import java.time.LocalDateTime;
public interface PagamentoVendaRepository extends JpaRepository<PagamentoVenda,Long>{
 @Query("SELECT p.forma, COALESCE(SUM(p.valor),0) FROM PagamentoVenda p WHERE p.venda.loja.id=:lojaId "
         + "AND p.venda.status<>com.lojaagro.estoque_api.entities.StatusVenda.CANCELADA GROUP BY p.forma")
 List<Object[]> somarPorForma(Long lojaId);
 @Query("SELECT p.forma,COALESCE(SUM(p.valor),0) FROM PagamentoVenda p WHERE p.venda.loja.id=:lojaId "
         + "AND p.venda.criadaEm BETWEEN :inicio AND :fim "
         + "AND p.venda.status<>com.lojaagro.estoque_api.entities.StatusVenda.CANCELADA GROUP BY p.forma")
 List<Object[]> somarPorFormaPeriodo(Long lojaId, LocalDateTime inicio, LocalDateTime fim);
}
