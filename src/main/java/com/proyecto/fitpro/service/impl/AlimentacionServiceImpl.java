package com.proyecto.fitpro.service.impl;

import com.proyecto.fitpro.model.Alimentacion;
import com.proyecto.fitpro.repository.AlimentacionRepository;
import com.proyecto.fitpro.service.AlimentacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AlimentacionServiceImpl implements AlimentacionService {

    @Autowired
    private AlimentacionRepository alimentacionRepository;

    @Override
    public Alimentacion crear(Alimentacion alimentacion) {
        return alimentacionRepository.save(alimentacion);
    }

    @Override
    public List<Alimentacion> obtenerTodas() {
        return alimentacionRepository.findAll();
    }

    @Override
    public List<Alimentacion> obtenerPorCliente(Integer idCliente) {
        return alimentacionRepository.findByCliente_IdCliente(idCliente);
    }

    @Override
    public Optional<Alimentacion> obtenerPorId(Integer id) {
        return alimentacionRepository.findById(id);
    }

    @Override
    public Alimentacion actualizar(Alimentacion alimentacion) {
        return alimentacionRepository.save(alimentacion);
    }

    @Override
    public void eliminar(Integer id) {
        alimentacionRepository.deleteById(id);
    }
}
