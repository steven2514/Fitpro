package com.proyecto.fitpro.service.impl;

import com.proyecto.fitpro.model.Entrenador;
import com.proyecto.fitpro.repository.EntrenadorRepository;
import com.proyecto.fitpro.service.EntrenadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EntrenadorServiceImpl implements EntrenadorService {

    @Autowired
    private EntrenadorRepository entrenadorRepository;

    @Override
    public Entrenador crear(Entrenador entrenador) {
        return entrenadorRepository.save(entrenador);
    }

    @Override
    public List<Entrenador> obtenerTodos() {
        return entrenadorRepository.findAll();
    }

    @Override
    public Optional<Entrenador> obtenerPorId(Integer id) {
        return entrenadorRepository.findById(id);
    }

    @Override
    public Entrenador actualizar(Entrenador entrenador) {
        return entrenadorRepository.save(entrenador);
    }

    @Override
    public void eliminar(Integer id) {
        entrenadorRepository.deleteById(id);
    }
}
