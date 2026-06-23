package com.proyecto.fitpro.service;

import com.proyecto.fitpro.model.Clase;
import java.util.List;
import java.util.Optional;

public interface ClaseService {

    Clase crear(Clase clase);

    List<Clase> obtenerTodas();

    Optional<Clase> obtenerPorId(Integer id);

    Clase actualizar(Clase clase);

    void eliminar(Integer id);
}
