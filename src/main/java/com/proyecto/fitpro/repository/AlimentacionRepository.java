package com.proyecto.fitpro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.proyecto.fitpro.model.Alimentacion;

@Repository
public interface AlimentacionRepository extends JpaRepository<Alimentacion, Integer> {
    List<Alimentacion> findByCliente_IdCliente(Integer idCliente);
}
