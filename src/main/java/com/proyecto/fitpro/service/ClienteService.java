package com.proyecto.fitpro.service;

import com.proyecto.fitpro.dto.ClienteDTO;
import com.proyecto.fitpro.dto.DatosFisicosDTO;
import com.proyecto.fitpro.dto.PerfilDTO;
import com.proyecto.fitpro.model.Cliente;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Optional;

public interface ClienteService {
    Cliente registrar(ClienteDTO datos);
    List<Cliente> obtenerTodos();
    Page<Cliente> buscar(String texto, int pagina, int tamano);
    long contar();
    Optional<Cliente> obtenerPorId(Integer id);
    Optional<Cliente> obtenerPorDocumento(String documento);
    Cliente actualizarCuenta(Integer id, ClienteDTO datos);
    /** Cambios de contacto hechos por el propio cliente. */
    Cliente actualizarPerfil(Integer id, PerfilDTO datos);
    /** Exige la contraseña actual correcta. */
    void cambiarPassword(Integer id, String actual, String nueva);
    /** Actualiza los datos físicos y guarda la medición en el historial de progreso. */
    Cliente actualizarDatosFisicos(Integer id, DatosFisicosDTO datos);
    /** Inscripción hecha por un administrador: sólo comprueba cupos. */
    void inscribirEnClase(Integer idCliente, Integer idClase);
    /** Inscripción hecha por el propio cliente: además exige un plan vigente que incluya clases. */
    void inscribirsePorCuenta(Integer idCliente, Integer idClase);
    void cancelarInscripcion(Integer idCliente, Integer idClase);
    void eliminar(Integer id);
    Optional<Cliente> obtenerPorIdConClases(Integer id);
}
