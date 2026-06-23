package com.proyecto.fitpro.service;

import com.proyecto.fitpro.model.Alimentacion;
import java.util.List;
import java.util.Optional;

public interface AlimentacionService {

    Alimentacion crear(Alimentacion alimentacion);

    List<Alimentacion> obtenerTodas();

    List<Alimentacion> obtenerPorCliente(Integer idCliente);

    Optional<Alimentacion> obtenerPorId(Integer id);

    Alimentacion actualizar(Alimentacion alimentacion);

    void eliminar(Integer id);

}
