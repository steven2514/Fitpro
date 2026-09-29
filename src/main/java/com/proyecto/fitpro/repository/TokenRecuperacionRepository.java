package com.proyecto.fitpro.repository;

import com.proyecto.fitpro.model.TokenRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Integer> {
    Optional<TokenRecuperacion> findByTokenHash(String tokenHash);
    List<TokenRecuperacion> findByTipoUsuarioAndIdUsuarioAndUsadoFalse(TokenRecuperacion.TipoUsuario tipo, Integer idUsuario);
}
