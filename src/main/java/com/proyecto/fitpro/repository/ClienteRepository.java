package com.proyecto.fitpro.repository;

import com.proyecto.fitpro.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /** Búsqueda paginada por nombre, apellido, documento o email (sin distinguir mayúsculas). */
    @Query("""
        SELECT c FROM Cliente c
        WHERE :q IS NULL
           OR LOWER(CONCAT(c.nombre, ' ', c.apellido)) LIKE LOWER(CONCAT('%', :q, '%'))
           OR c.documento LIKE CONCAT('%', :q, '%')
           OR LOWER(c.email) LIKE LOWER(CONCAT('%', :q, '%'))
        """)
    Page<Cliente> buscar(@Param("q") String q, Pageable pageable);
}
