package com.proyecto.fitpro.repository;

import com.proyecto.fitpro.model.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Integer> {

    List<Suscripcion> findByCliente_IdClienteAndEstado(Integer idCliente, Suscripcion.Estado estado);

    List<Suscripcion> findByCliente_IdClienteOrderByFechaInicioDesc(Integer idCliente);

    boolean existsByPlan_IdPlan(Integer idPlan);

    void deleteByCliente_IdCliente(Integer idCliente);

    @Query("""
        SELECT s FROM Suscripcion s JOIN FETCH s.cliente JOIN FETCH s.plan
        WHERE s.estado = :estado AND s.fechaFin >= :hoy
        ORDER BY s.fechaFin ASC
        """)
    List<Suscripcion> findPorEstadoVigentesDesde(@Param("estado") Suscripcion.Estado estado, @Param("hoy") LocalDate hoy);

    long countByPlan_IdPlanAndEstadoAndFechaFinGreaterThanEqual(Integer idPlan, Suscripcion.Estado estado, LocalDate hoy);

    /** Suscripciones activas y dentro de su periodo, con cliente y plan ya cargados para listarlas. */
    default List<Suscripcion> findVigentes(LocalDate hoy) {
        return findPorEstadoVigentesDesde(Suscripcion.Estado.ACTIVA, hoy);
    }

    default long countVigentesPorPlan(Integer idPlan, LocalDate hoy) {
        return countByPlan_IdPlanAndEstadoAndFechaFinGreaterThanEqual(idPlan, Suscripcion.Estado.ACTIVA, hoy);
    }
}
