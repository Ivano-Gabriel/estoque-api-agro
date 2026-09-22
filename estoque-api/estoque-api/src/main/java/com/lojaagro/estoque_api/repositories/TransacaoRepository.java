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
    
    List<Transacao> findByLojaIdAndProdutoId(Long lojaId, Long produtoId);
    
    List<Transacao> findByLojaIdAndTipo(Long lojaId, String tipo);
    
    @Query("SELECT t FROM Transacao t WHERE t.loja.id = :lojaId AND t.data BETWEEN :inicio AND :fim ORDER BY t.data DESC")
    List<Transacao> findByPeriodo(@Param("lojaId") Long lojaId, @Param("inicio") LocalDateTime inicio,
                                  @Param("fim") LocalDateTime fim);
    
    @Query("SELECT SUM(t.valorTotal) FROM Transacao t WHERE t.loja.id = :lojaId AND t.tipo = :tipo")
    BigDecimal sumValorTotalByTipo(@Param("lojaId") Long lojaId, @Param("tipo") String tipo);

    @Query("SELECT SUM(t.lucro) FROM Transacao t WHERE t.loja.id = :lojaId AND t.tipo = 'VENDA'")
    BigDecimal sumLucroVendas(@Param("lojaId") Long lojaId);
    
    List<Transacao> findTop10ByLojaIdOrderByDataDesc(Long lojaId);

    List<Transacao> findByLojaIdOrderByDataDesc(Long lojaId);
}
