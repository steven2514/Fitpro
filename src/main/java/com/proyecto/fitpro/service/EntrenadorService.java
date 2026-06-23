package com.proyecto.fitpro.service;

import com.proyecto.fitpro.model.Entrenador;
import java.util.List;
import java.util.Optional;

public interface EntrenadorService {
    Entrenador crear(Entrenador entrenador);
    List<Entrenador> obtenerTodos();

    Optional<Entrenador> obtenerPorId(Integer id);

    Entrenador actualizar(Entrenador entrenador);

    void eliminar(Integer id);
}
