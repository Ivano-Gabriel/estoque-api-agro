package com.lojaagro.estoque_api.repositories;

import com.lojaagro.estoque_api.entities.LimiteLogin;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Optional;

public interface LimiteLoginRepository extends JpaRepository<LimiteLogin, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM LimiteLogin l WHERE l.id = :id")
    Optional<LimiteLogin> bloquear(@Param("id") String id);

    long deleteByInicioBeforeAndIdNot(Instant limite, String id);
}
