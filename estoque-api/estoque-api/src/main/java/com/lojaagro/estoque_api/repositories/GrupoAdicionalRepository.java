package com.lojaagro.estoque_api.repositories;
import com.lojaagro.estoque_api.entities.*;import org.springframework.data.jpa.repository.*;import java.util.*;
public interface GrupoAdicionalRepository extends JpaRepository<GrupoAdicional,Long>{@EntityGraph(attributePaths={"opcoes","opcoes.produto","opcoes.ingrediente"})List<GrupoAdicional>findByLojaIdOrderByOrdemAscIdAsc(Long lojaId);@EntityGraph(attributePaths={"opcoes","opcoes.produto","opcoes.ingrediente"})Optional<GrupoAdicional>findByIdAndLojaId(Long id,Long lojaId);}
