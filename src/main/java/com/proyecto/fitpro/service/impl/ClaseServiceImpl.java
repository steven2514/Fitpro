package com.proyecto.fitpro.service.impl;

import com.proyecto.fitpro.model.Clase;
import com.proyecto.fitpro.repository.ClaseRepository;
import com.proyecto.fitpro.service.ClaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClaseServiceImpl implements ClaseService {

    @Autowired
    private ClaseRepository claseRepository;

    @Override
    public Clase crear(Clase clase) {
        return claseRepository.save(clase);
    }

    @Override
    public List<Clase> obtenerTodas() {
        return claseRepository.findAll();
    }

    @Override
    public Optional<Clase> obtenerPorId(Integer id) {
        return claseRepository.findById(id);
    }

    @Override
    public Clase actualizar(Clase clase) {
        return claseRepository.save(clase);
    }

    @Override
    public void eliminar(Integer id) {
        claseRepository.deleteById(id);
    }
}
