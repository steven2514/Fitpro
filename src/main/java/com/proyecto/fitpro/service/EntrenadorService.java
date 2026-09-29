package com.proyecto.fitpro.service;

import com.proyecto.fitpro.model.Clase;
import com.proyecto.fitpro.model.Cliente;
import com.proyecto.fitpro.model.Entrenador;
import com.proyecto.fitpro.model.Rutina;
import java.util.List;
import java.util.Optional;

public interface EntrenadorService {
    Entrenador crear(Entrenador entrenador);

    List<Entrenador> obtenerTodos();

    Optional<Entrenador> obtenerPorId(Integer id);

    Entrenador actualizar(Integer id, Entrenador datos);

    void eliminar(Integer id);

    /** Da (o cambia) la contraseña del entrenador para que pueda iniciar sesión con su email. */
    void establecerAcceso(Integer id, String password);

    void quitarAcceso(Integer id);

    List<Clase> obtenerClases(Integer idEntrenador);

    /** Clientes inscritos en alguna clase del entrenador, sin repetir y ordenados por nombre. */
    List<Cliente> obtenerAlumnos(Integer idEntrenador);

    List<Rutina> obtenerRutinas(Integer idEntrenador);

    /** Crea una rutina para uno de sus alumnos; falla si el cliente no está en sus clases. */
    Rutina crearRutina(Integer idEntrenador, Integer idCliente, String nombre, String objetivo, String nivel);

    /** Comprueba que la rutina es del entrenador (para que no edite las de otros). */
    Rutina obtenerRutinaPropia(Integer idEntrenador, Integer idRutina);
}
