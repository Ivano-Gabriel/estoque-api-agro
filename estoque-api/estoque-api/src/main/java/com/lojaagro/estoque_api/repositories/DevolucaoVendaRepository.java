package com.lojaagro.estoque_api.repositories;
import com.lojaagro.estoque_api.entities.DevolucaoVenda; import org.springframework.data.jpa.repository.*; import java.util.*; import java.time.LocalDateTime;
public interface DevolucaoVendaRepository extends JpaRepository<DevolucaoVenda,UUID>{
 @Query("SELECT d.formaReembolso,COALESCE(SUM(d.valor),0) FROM DevolucaoVenda d WHERE d.loja.id=:lojaId GROUP BY d.formaReembolso") List<Object[]> somarPorForma(Long lojaId);
 @Query("SELECT d.formaReembolso,COALESCE(SUM(d.valor),0) FROM DevolucaoVenda d WHERE d.loja.id=:lojaId AND d.criadaEm BETWEEN :inicio AND :fim GROUP BY d.formaReembolso") List<Object[]> somarPorFormaPeriodo(Long lojaId,LocalDateTime inicio,LocalDateTime fim);
 List<DevolucaoVenda> findByVendaIdOrderByCriadaEmDesc(UUID vendaId);
}
