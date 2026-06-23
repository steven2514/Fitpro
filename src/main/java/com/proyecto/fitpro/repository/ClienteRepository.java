package com.proyecto.fitpro.repository;

import com.proyecto.fitpro.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByDocumento(String documento);
    Optional<Cliente> findByEmail(String email);

    @Query("SELECT c FROM Cliente c LEFT JOIN FETCH c.clases WHERE c.idCliente = :id")
    Optional<Cliente> findByIdWithClases(@Param("id") Integer id);
}
