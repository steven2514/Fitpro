package com.proyecto.fitpro.service.impl;

import com.proyecto.fitpro.model.Rutina;
import com.proyecto.fitpro.repository.RutinaRepository;
import com.proyecto.fitpro.service.RutinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RutinaServiceImpl implements RutinaService {

    @Autowired
    private RutinaRepository rutinaRepository;

    @Override
    public Rutina crear(Rutina rutina) {
        return rutinaRepository.save(rutina);
    }

    @Override
    public List<Rutina> obtenerTodas() {
        return rutinaRepository.findAll();
    }

    @Override
    public List<Rutina> obtenerPorCliente(Integer idCliente) {
        return rutinaRepository.findByCliente_IdCliente(idCliente);
    }

    @Override
    public Optional<Rutina> obtenerPorId(Integer id) {
        return rutinaRepository.findById(id);
    }

    @Override
    public Rutina actualizar(Rutina rutina) {
        return rutinaRepository.save(rutina);
    }

    @Override
    public void eliminar(Integer id) {
        rutinaRepository.deleteById(id);
    }
}
