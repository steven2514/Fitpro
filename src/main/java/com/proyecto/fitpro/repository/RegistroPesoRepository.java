package com.proyecto.fitpro.repository;

import com.proyecto.fitpro.model.RegistroPeso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroPesoRepository extends JpaRepository<RegistroPeso, Integer> {
    List<RegistroPeso> findByCliente_IdClienteOrderByFechaAsc(Integer idCliente);
    Optional<RegistroPeso> findByCliente_IdClienteAndFecha(Integer idCliente, LocalDate fecha);
    void deleteByCliente_IdCliente(Integer idCliente);
}
