package com.lojaagro.estoque_api.repositories;

import com.lojaagro.estoque_api.entities.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    Page<Auditoria> findByLojaId(Long lojaId, Pageable pageable);
}
