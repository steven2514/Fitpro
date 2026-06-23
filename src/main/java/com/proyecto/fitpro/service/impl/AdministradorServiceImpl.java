package com.proyecto.fitpro.service.impl;

import com.proyecto.fitpro.model.Administrador;
import com.proyecto.fitpro.repository.AdministradorRepository;
import com.proyecto.fitpro.service.AdministradorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdministradorServiceImpl implements AdministradorService {

    @Autowired
    private AdministradorRepository administradorRepository;

    @Override
    public Administrador crear(Administrador administrador) {
        return administradorRepository.save(administrador);
    }

    @Override
    public List<Administrador> obtenerTodos() {
        return administradorRepository.findAll();
    }

    @Override
    public Optional<Administrador> obtenerPorId(Integer id) {
        return administradorRepository.findById(id);
    }

    @Override
    public Optional<Administrador> obtenerPorEmail(String email) {
        return administradorRepository.findByEmail(email);
    }

    @Override
    public Administrador actualizar(Administrador administrador) {
        return administradorRepository.save(administrador);
    }

    @Override
    public void eliminar(Integer id) {
        administradorRepository.deleteById(id);
    }
}
