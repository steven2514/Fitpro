package com.proyecto.fitpro.service.impl;

import com.proyecto.fitpro.dto.PlanDTO;
import com.proyecto.fitpro.exception.NegocioException;
import com.proyecto.fitpro.model.Plan;
import com.proyecto.fitpro.repository.PlanRepository;
import com.proyecto.fitpro.repository.SuscripcionRepository;
import com.proyecto.fitpro.service.PlanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final SuscripcionRepository suscripcionRepository;

    public PlanServiceImpl(PlanRepository planRepository, SuscripcionRepository suscripcionRepository) {
        this.planRepository = planRepository;
        this.suscripcionRepository = suscripcionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plan> obtenerActivos() {
        return planRepository.findByActivoTrueOrderByPrecioMensualAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plan> obtenerTodos() {
        return planRepository.findAllByOrderByPrecioMensualAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Plan> obtenerPorId(Integer id) {
        return planRepository.findById(id);
    }

    @Override
    public Plan crear(PlanDTO datos) {
        Plan plan = new Plan();
        datos.aplicarA(plan);
        plan.setActivo(true);
        return planRepository.save(plan);
    }

    @Override
    public Plan actualizar(Integer id, PlanDTO datos) {
        Plan plan = buscar(id);
        datos.aplicarA(plan);
        return plan;
    }

    @Override
    public Plan cambiarEstado(Integer id) {
        Plan plan = buscar(id);
        plan.setActivo(!plan.isActivo());
        return plan;
    }

    @Override
    public void eliminar(Integer id) {
        if (suscripcionRepository.existsByPlan_IdPlan(id)) {
            throw new NegocioException("El plan tiene suscripciones registradas: desactívalo en lugar de eliminarlo.");
        }
        planRepository.delete(buscar(id));
    }

    private Plan buscar(Integer id) {
        return planRepository.findById(id).orElseThrow(() -> new NegocioException("El plan no existe"));
    }
}
