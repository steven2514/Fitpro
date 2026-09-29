package com.proyecto.fitpro.repository;

import com.proyecto.fitpro.model.Entrenador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntrenadorRepository extends JpaRepository<Entrenador, Integer> {
    /** Sólo entrenadores con acceso: el email identifica al entrenador en el login. */
    Optional<Entrenador> findFirstByEmailAndPasswordIsNotNull(String email);
    List<Entrenador> findByEmail(String email);
}
