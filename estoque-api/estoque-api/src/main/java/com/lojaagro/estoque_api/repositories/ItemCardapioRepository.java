package com.lojaagro.estoque_api.repositories;
import com.lojaagro.estoque_api.entities.*;import org.springframework.data.jpa.repository.*;import java.util.*;
public interface ItemCardapioRepository extends JpaRepository<ItemCardapio,Long>{
 @EntityGraph(attributePaths={"produto","produto.categoria"}) List<ItemCardapio> findByLojaIdOrderByOrdemAscIdAsc(Long lojaId);
 @EntityGraph(attributePaths={"produto"}) Optional<ItemCardapio> findByIdAndLojaId(Long id,Long lojaId);
}
