package com.proyecto.fitpro.service.impl;

import com.proyecto.fitpro.dto.HorarioSemanal;
import com.proyecto.fitpro.exception.NegocioException;
import com.proyecto.fitpro.model.Clase;
import com.proyecto.fitpro.model.Cliente;
import com.proyecto.fitpro.model.Entrenador;
import com.proyecto.fitpro.model.Rutina;
import com.proyecto.fitpro.repository.AdministradorRepository;
import com.proyecto.fitpro.repository.ClaseRepository;
import com.proyecto.fitpro.repository.ClienteRepository;
import com.proyecto.fitpro.repository.EntrenadorRepository;
import com.proyecto.fitpro.repository.RutinaRepository;
import com.proyecto.fitpro.service.EntrenadorService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class EntrenadorServiceImpl implements EntrenadorService {

    private final EntrenadorRepository entrenadorRepository;
    private final ClaseRepository claseRepository;
    private final RutinaRepository rutinaRepository;
    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;

    public EntrenadorServiceImpl(EntrenadorRepository entrenadorRepository, ClaseRepository claseRepository,
            RutinaRepository rutinaRepository, ClienteRepository clienteRepository,
            AdministradorRepository administradorRepository, PasswordEncoder passwordEncoder) {
        this.entrenadorRepository = entrenadorRepository;
        this.claseRepository = claseRepository;
        this.rutinaRepository = rutinaRepository;
        this.clienteRepository = clienteRepository;
        this.administradorRepository = administradorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Entrenador crear(Entrenador entrenador) {
        entrenador.setIdEntrenador(null);
        entrenador.setPassword(null);
        return entrenadorRepository.save(entrenador);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Entrenador> obtenerTodos() {
        return entrenadorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Entrenador> obtenerPorId(Integer id) {
        return entrenadorRepository.findById(id);
    }

    @Override
    public Entrenador actualizar(Integer id, Entrenador datos) {
        Entrenador entrenador = buscar(id);
        if (entrenador.isTieneAcceso() && !Objects.equals(entrenador.getEmail(), datos.getEmail())) {
            validarEmailParaAcceso(datos.getEmail(), id);
        }
        entrenador.setNombre(datos.getNombre());
        entrenador.setEspecialidad(datos.getEspecialidad());
        entrenador.setHorario(datos.getHorario());
        entrenador.setEmail(datos.getEmail());
        entrenador.setTelefono(datos.getTelefono());
        return entrenador;
    }

    @Override
    public void eliminar(Integer id) {
        long clases = claseRepository.countByEntrenador_IdEntrenador(id);
        if (clases > 0) {
            throw new NegocioException("No se puede eliminar: el entrenador tiene " + clases
                + " clase(s) asignada(s). Reasígnalas o elimínalas primero.");
        }
        // Sus rutinas quedan como creadas por la administración
        rutinaRepository.findByEntrenador_IdEntrenadorOrderByIdRutinaDesc(id).forEach(r -> r.setEntrenador(null));
        entrenadorRepository.deleteById(id);
    }

    @Override
    public void establecerAcceso(Integer id, String password) {
        Entrenador entrenador = buscar(id);
        validarEmailParaAcceso(entrenador.getEmail(), id);
        entrenador.setPassword(passwordEncoder.encode(password));
    }

    @Override
    public void quitarAcceso(Integer id) {
        buscar(id).setPassword(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Clase> obtenerClases(Integer idEntrenador) {
        return claseRepository.findByEntrenador_IdEntrenador(idEntrenador).stream()
            .sorted(HorarioSemanal.POR_HORARIO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> obtenerAlumnos(Integer idEntrenador) {
        Map<Integer, Cliente> alumnos = new LinkedHashMap<>();
        claseRepository.findByEntrenador_IdEntrenador(idEntrenador).forEach(clase -> {
            if (clase.getClientes() != null) {
                clase.getClientes().forEach(c -> alumnos.putIfAbsent(c.getIdCliente(), c));
            }
        });
        return alumnos.values().stream()
            .sorted(Comparator.comparing(Cliente::getNombre, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rutina> obtenerRutinas(Integer idEntrenador) {
        return rutinaRepository.findByEntrenador_IdEntrenadorOrderByIdRutinaDesc(idEntrenador);
    }

    @Override
    public Rutina crearRutina(Integer idEntrenador, Integer idCliente, String nombre, String objetivo, String nivel) {
        Entrenador entrenador = buscar(idEntrenador);
        boolean esAlumno = obtenerAlumnos(idEntrenador).stream().anyMatch(c -> c.getIdCliente().equals(idCliente));
        if (!esAlumno) {
            throw new NegocioException("Sólo puedes crear rutinas para clientes inscritos en tus clases");
        }
        Rutina rutina = new Rutina();
        rutina.setEntrenador(entrenador);
        rutina.setCliente(clienteRepository.getReferenceById(idCliente));
        rutina.setNombre(nombre);
        rutina.setObjetivo(objetivo);
        rutina.setNivel(nivel);
        return rutinaRepository.save(rutina);
    }

    @Override
    @Transactional(readOnly = true)
    public Rutina obtenerRutinaPropia(Integer idEntrenador, Integer idRutina) {
        Rutina rutina = rutinaRepository.findById(idRutina)
            .orElseThrow(() -> new NegocioException("La rutina no existe"));
        if (rutina.getEntrenador() == null || !rutina.getEntrenador().getIdEntrenador().equals(idEntrenador)) {
            throw new NegocioException("Esta rutina no es tuya");
        }
        return rutina;
    }

    private Entrenador buscar(Integer id) {
        return entrenadorRepository.findById(id)
            .orElseThrow(() -> new NegocioException("El entrenador no existe"));
    }

    /** El login busca por email en clientes, luego admins y luego entrenadores: el email no puede repetirse. */
    private void validarEmailParaAcceso(String email, Integer idActual) {
        if (email == null || email.isBlank()) {
            throw new NegocioException("El entrenador necesita un email para poder iniciar sesión");
        }
        boolean usadoPorOtroEntrenador = entrenadorRepository.findByEmail(email).stream()
            .anyMatch(e -> !e.getIdEntrenador().equals(idActual) && e.isTieneAcceso());
        if (usadoPorOtroEntrenador || clienteRepository.findByEmail(email).isPresent()
                || administradorRepository.findByEmail(email).isPresent()) {
            throw new NegocioException("Ese email ya lo usa otra cuenta; usa uno distinto para el entrenador");
        }
    }
}
