package com.proyecto.fitpro.service;

import com.proyecto.fitpro.model.Rutina;
import java.util.List;
import java.util.Optional;

public interface RutinaService {

    Rutina crear(Rutina rutina);

    List<Rutina> obtenerTodas();

    List<Rutina> obtenerPorCliente(Integer idCliente);

    Optional<Rutina> obtenerPorId(Integer id);

    Rutina actualizar(Rutina rutina);

    void eliminar(Integer id);
}
