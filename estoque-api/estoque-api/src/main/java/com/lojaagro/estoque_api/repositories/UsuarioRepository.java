package com.lojaagro.estoque_api.repositories;

import com.lojaagro.estoque_api.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    java.util.List<Usuario> findByLojaIdAndRole(Long lojaId, com.lojaagro.estoque_api.entities.UsuarioRole role);

    Optional<Usuario> findByIdAndLojaId(Long id, Long lojaId);
    java.util.List<Usuario> findByLojaId(Long lojaId);
    
}
