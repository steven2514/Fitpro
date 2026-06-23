package com.proyecto.fitpro.service;

import com.proyecto.fitpro.model.Administrador;
import java.util.List;
import java.util.Optional;

public interface AdministradorService {
    Administrador crear(Administrador administrador);
    List<Administrador> obtenerTodos();
    Optional<Administrador> obtenerPorId(Integer id);
    Optional<Administrador> obtenerPorEmail(String email);
    Administrador actualizar(Administrador administrador);
    void eliminar(Integer id);
}
