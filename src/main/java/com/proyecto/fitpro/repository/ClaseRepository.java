package com.proyecto.fitpro.repository;

import com.proyecto.fitpro.model.Clase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClaseRepository extends JpaRepository<Clase, Integer> {
    long countByEntrenador_IdEntrenador(Integer idEntrenador);
    List<Clase> findByEntrenador_IdEntrenador(Integer idEntrenador);
}
