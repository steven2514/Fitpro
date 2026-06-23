package com.proyecto.fitpro.service;

import com.proyecto.fitpro.model.Cliente;
import java.util.List;
import java.util.Optional;

public interface ClienteService {
    Cliente crear(Cliente cliente);
    List<Cliente> obtenerTodos();
    Optional<Cliente> obtenerPorId(Integer id);
    Optional<Cliente> obtenerPorDocumento(String documento);
    Cliente actualizar(Cliente cliente);
    void eliminar(Integer id);
    Optional<Cliente> obtenerPorIdConClases(Integer id);
}
