package com.proyecto.fitpro.service;

import com.proyecto.fitpro.dto.EjercicioDTO;
import com.proyecto.fitpro.model.Ejercicio;
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

    List<Ejercicio> obtenerEjercicios(Integer idRutina);

    Ejercicio agregarEjercicio(Integer idRutina, EjercicioDTO datos);

    /** Elimina el ejercicio y devuelve el id de su rutina, para volver a ella. */
    Integer eliminarEjercicio(Integer idEjercicio);
}
