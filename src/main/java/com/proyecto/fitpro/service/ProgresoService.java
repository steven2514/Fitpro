package com.proyecto.fitpro.service;

import com.proyecto.fitpro.model.RegistroPeso;
import java.util.List;

public interface ProgresoService {
    void registrar(Integer idCliente, Double peso, Double altura);
    List<RegistroPeso> obtenerHistorial(Integer idCliente);
}
