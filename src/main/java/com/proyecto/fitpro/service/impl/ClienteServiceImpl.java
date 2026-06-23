package com.proyecto.fitpro.service.impl;

import com.proyecto.fitpro.model.Cliente;
import com.proyecto.fitpro.repository.ClienteRepository;
import com.proyecto.fitpro.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Override
    public Cliente crear(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    @Override
    public List<Cliente> obtenerTodos() {
        return clienteRepository.findAll();
    }

    @Override
    public Optional<Cliente> obtenerPorId(Integer id) {
        return clienteRepository.findById(id);
    }

    @Override
    public Optional<Cliente> obtenerPorDocumento(String documento) {
        return clienteRepository.findByDocumento(documento);
    }

    @Override
    public Cliente actualizar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    @Override
    public void eliminar(Integer id) {
        clienteRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> obtenerPorIdConClases(Integer id) {
        return clienteRepository.findByIdWithClases(id);
    }
}
