package com.lojaagro.estoque_api.repositories;

import com.lojaagro.estoque_api.entities.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    
    List<Transacao> findByProdutoId(Long produtoId);
    
    List<Transacao> findByTipo(String tipo);
    
    @Query("SELECT t FROM Transacao t WHERE t.data BETWEEN :inicio AND :fim ORDER BY t.data DESC")
    List<Transacao> findByPeriodo(@Param("inicio") LocalDateTime inicio, 
                                  @Param("fim") LocalDateTime fim);
    
    @Query("SELECT SUM(t.valorTotal) FROM Transacao t WHERE t.tipo = :tipo")
    BigDecimal sumValorTotalByTipo(@Param("tipo") String tipo);

    @Query("SELECT SUM(t.lucro) FROM Transacao t WHERE t.tipo = 'VENDA'")
    BigDecimal sumLucroVendas();
    
    @Query("SELECT t FROM Transacao t ORDER BY t.data DESC")
    List<Transacao> findTop10ByOrderByDataDesc();
}
