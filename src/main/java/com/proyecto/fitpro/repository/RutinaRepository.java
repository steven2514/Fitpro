package com.proyecto.fitpro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.fitpro.model.Rutina;
import java.util.List;

public interface RutinaRepository extends JpaRepository<Rutina, Integer> {
    List<Rutina> findByCliente_IdCliente(Integer idCliente);
}
