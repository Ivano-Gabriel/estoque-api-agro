package com.lojaagro.estoque_api.repositories;

import com.lojaagro.estoque_api.entities.Loja;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LojaRepository extends JpaRepository<Loja, Long> {
    Optional<Loja> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
