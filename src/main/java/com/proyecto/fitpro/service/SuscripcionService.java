package com.proyecto.fitpro.service;

import com.proyecto.fitpro.model.Suscripcion;
import java.util.List;
import java.util.Optional;

public interface SuscripcionService {
    /** Suscribe al cliente al plan. Si ya tenía una suscripción activa, la reemplaza (cambio de plan). */
    Suscripcion suscribir(Integer idCliente, Integer idPlan);
    Optional<Suscripcion> obtenerVigente(Integer idCliente);
    List<Suscripcion> obtenerHistorial(Integer idCliente);
    List<Suscripcion> obtenerVigentes();
    long contarVigentesPorPlan(Integer idPlan);
    /** Suma de lo que pagan al mes las suscripciones vigentes. */
    long ingresosMensuales();
    void cancelarVigente(Integer idCliente);
    void cancelar(Integer idSuscripcion);
    /** Vigentes a las que les quedan pocos días (ver Suscripcion.DIAS_AVISO). */
    List<Suscripcion> obtenerPorVencer();
    /** Envía un correo a cada cliente con el plan por vencer que aún no fue avisado. Devuelve cuántos se avisaron. */
    int enviarAvisosVencimiento();
}
