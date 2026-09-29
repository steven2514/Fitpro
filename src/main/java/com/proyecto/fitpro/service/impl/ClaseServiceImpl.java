package com.proyecto.fitpro.service.impl;

import com.proyecto.fitpro.exception.NegocioException;
import com.proyecto.fitpro.model.Clase;
import com.proyecto.fitpro.repository.ClaseRepository;
import com.proyecto.fitpro.service.ClaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClaseServiceImpl implements ClaseService {

    private final ClaseRepository claseRepository;

    public ClaseServiceImpl(ClaseRepository claseRepository) {
        this.claseRepository = claseRepository;
    }

    @Override
    public Clase crear(Clase clase) {
        return claseRepository.save(clase);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Clase> obtenerTodas() {
        return claseRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Clase> obtenerPorId(Integer id) {
        return claseRepository.findById(id);
    }

    @Override
    public Clase actualizar(Clase clase) {
        return claseRepository.save(clase);
    }

    @Override
    public void eliminar(Integer id) {
        Clase clase = claseRepository.findById(id)
            .orElseThrow(() -> new NegocioException("La clase no existe"));
        // La inscripción la guarda el lado Cliente: hay que quitarla ahí antes de borrar la clase
        if (clase.getClientes() != null) {
            clase.getClientes().forEach(cliente -> cliente.getClases().remove(clase));
        }
        claseRepository.delete(clase);
    }
}
