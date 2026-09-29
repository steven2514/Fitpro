package com.proyecto.fitpro.repository;

import com.proyecto.fitpro.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Integer> {
    List<Plan> findByActivoTrueOrderByPrecioMensualAsc();
    List<Plan> findAllByOrderByPrecioMensualAsc();
}
