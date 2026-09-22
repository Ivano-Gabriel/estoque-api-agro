package com.lojaagro.estoque_api.repositories;

import com.lojaagro.estoque_api.entities.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findByLojaIdAndNomeIgnoreCase(Long lojaId, String nome);
    Optional<Categoria> findByIdAndLojaId(Long id, Long lojaId);
    java.util.List<Categoria> findByLojaIdOrderByNome(Long lojaId);
}
