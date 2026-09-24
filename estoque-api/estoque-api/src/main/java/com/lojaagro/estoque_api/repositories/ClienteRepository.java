package com.lojaagro.estoque_api.repositories;

import com.lojaagro.estoque_api.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByLojaIdAndAtivoTrueOrderByNomeAsc(Long lojaId);
    Optional<Cliente> findByIdAndLojaIdAndAtivoTrue(Long id, Long lojaId);
}
