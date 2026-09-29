package com.proyecto.fitpro.service;

import com.proyecto.fitpro.dto.AdministradorDTO;
import com.proyecto.fitpro.model.Administrador;
import java.util.List;
import java.util.Optional;

public interface AdministradorService {
    Administrador registrar(AdministradorDTO datos);
    List<Administrador> obtenerTodos();
    Optional<Administrador> obtenerPorId(Integer id);
    Optional<Administrador> obtenerPorEmail(String email);
    void eliminar(Integer id);
}
