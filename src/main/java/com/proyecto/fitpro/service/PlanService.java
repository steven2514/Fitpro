package com.proyecto.fitpro.service;

import com.proyecto.fitpro.dto.PlanDTO;
import com.proyecto.fitpro.model.Plan;
import java.util.List;
import java.util.Optional;

public interface PlanService {
    List<Plan> obtenerActivos();
    List<Plan> obtenerTodos();
    Optional<Plan> obtenerPorId(Integer id);
    Plan crear(PlanDTO datos);
    Plan actualizar(Integer id, PlanDTO datos);
    /** Activa o desactiva el plan. Un plan inactivo no se ofrece a nuevos clientes, pero sus suscripciones siguen. */
    Plan cambiarEstado(Integer id);
    void eliminar(Integer id);
}
