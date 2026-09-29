package com.proyecto.fitpro.service.impl;

import com.proyecto.fitpro.dto.ClienteDTO;
import com.proyecto.fitpro.dto.DatosFisicosDTO;
import com.proyecto.fitpro.dto.PerfilDTO;
import com.proyecto.fitpro.model.TokenRecuperacion;
import com.proyecto.fitpro.repository.EntrenadorRepository;
import com.proyecto.fitpro.repository.TokenRecuperacionRepository;
import com.proyecto.fitpro.exception.NegocioException;
import com.proyecto.fitpro.model.Clase;
import com.proyecto.fitpro.model.Cliente;
import com.proyecto.fitpro.model.Suscripcion;
import com.proyecto.fitpro.repository.AdministradorRepository;
import com.proyecto.fitpro.repository.AlimentacionRepository;
import com.proyecto.fitpro.repository.ClaseRepository;
import com.proyecto.fitpro.repository.ClienteRepository;
import com.proyecto.fitpro.repository.RegistroPesoRepository;
import com.proyecto.fitpro.repository.RutinaRepository;
import com.proyecto.fitpro.repository.SuscripcionRepository;
import com.proyecto.fitpro.service.ClienteService;
import com.proyecto.fitpro.service.ProgresoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;
    private final RutinaRepository rutinaRepository;
    private final AlimentacionRepository alimentacionRepository;
    private final ClaseRepository claseRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final RegistroPesoRepository registroPesoRepository;
    private final ProgresoService progresoService;
    private final EntrenadorRepository entrenadorRepository;
    private final TokenRecuperacionRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteServiceImpl(ClienteRepository clienteRepository, AdministradorRepository administradorRepository,
            RutinaRepository rutinaRepository, AlimentacionRepository alimentacionRepository,
            ClaseRepository claseRepository, SuscripcionRepository suscripcionRepository,
            RegistroPesoRepository registroPesoRepository, ProgresoService progresoService,
            EntrenadorRepository entrenadorRepository, TokenRecuperacionRepository tokenRepository,
            PasswordEncoder passwordEncoder) {
        this.entrenadorRepository = entrenadorRepository;
        this.tokenRepository = tokenRepository;
        this.clienteRepository = clienteRepository;
        this.administradorRepository = administradorRepository;
        this.rutinaRepository = rutinaRepository;
        this.alimentacionRepository = alimentacionRepository;
        this.claseRepository = claseRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.registroPesoRepository = registroPesoRepository;
        this.progresoService = progresoService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Cliente registrar(ClienteDTO datos) {
        validarUnicos(datos, null);
        Cliente cliente = new Cliente();
        datos.aplicarA(cliente);
        if (datos.getPassword() != null) {
            cliente.setPassword(passwordEncoder.encode(datos.getPassword()));
        }
        return clienteRepository.save(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> obtenerTodos() {
        return clienteRepository.findAll(Sort.by("nombre", "apellido"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> buscar(String texto, int pagina, int tamano) {
        String q = (texto == null || texto.isBlank()) ? null : texto.trim();
        return clienteRepository.buscar(q, PageRequest.of(Math.max(0, pagina), tamano, Sort.by("idCliente").descending()));
    }

    @Override
    @Transactional(readOnly = true)
    public long contar() {
        return clienteRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> obtenerPorId(Integer id) {
        return clienteRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> obtenerPorDocumento(String documento) {
        return clienteRepository.findByDocumento(documento);
    }

    @Override
    public Cliente actualizarCuenta(Integer id, ClienteDTO datos) {
        Cliente cliente = buscar(id);
        validarUnicos(datos, id);
        datos.aplicarA(cliente);
        if (datos.getPassword() != null) {
            cliente.setPassword(passwordEncoder.encode(datos.getPassword()));
        }
        return cliente;
    }

    @Override
    public Cliente actualizarPerfil(Integer id, PerfilDTO datos) {
        Cliente cliente = buscar(id);
        if (datos.getEmail() != null) {
            validarEmailLibre(datos.getEmail(), id);
        }
        cliente.setEmail(datos.getEmail());
        cliente.setTelefono(datos.getTelefono());
        cliente.setDireccion(datos.getDireccion());
        return cliente;
    }

    @Override
    public void cambiarPassword(Integer id, String actual, String nueva) {
        Cliente cliente = buscar(id);
        if (actual == null || cliente.getPassword() == null || !passwordEncoder.matches(actual, cliente.getPassword())) {
            throw new NegocioException("La contraseña actual no es correcta");
        }
        if (passwordEncoder.matches(nueva, cliente.getPassword())) {
            throw new NegocioException("La nueva contraseña debe ser distinta de la actual");
        }
        cliente.setPassword(passwordEncoder.encode(nueva));
    }

    @Override
    public Cliente actualizarDatosFisicos(Integer id, DatosFisicosDTO datos) {
        Cliente cliente = buscar(id);
        cliente.setPeso(datos.getPeso());
        cliente.setAltura(datos.getAltura());
        cliente.setEdad(datos.getEdad());
        cliente.setGenero(datos.getGenero());
        cliente.setObjetivoFitness(datos.getObjetivoFitness());
        cliente.setNivelActividad(datos.getNivelActividad());
        progresoService.registrar(id, datos.getPeso(), datos.getAltura());
        return cliente;
    }

    @Override
    public void inscribirEnClase(Integer idCliente, Integer idClase) {
        Cliente cliente = buscar(idCliente);
        Clase clase = claseRepository.findById(idClase)
            .orElseThrow(() -> new NegocioException("La clase no existe"));
        if (cliente.getClases() == null) {
            cliente.setClases(new ArrayList<>());
        }
        if (cliente.getClases().stream().anyMatch(c -> c.getIdClase().equals(idClase))) {
            throw new NegocioException("Ya está inscrito en la clase \"" + clase.getNombre() + "\"");
        }
        if (clase.isLlena()) {
            throw new NegocioException("La clase \"" + clase.getNombre() + "\" no tiene cupos disponibles");
        }
        cliente.getClases().add(clase);
    }

    @Override
    public void inscribirsePorCuenta(Integer idCliente, Integer idClase) {
        boolean planConClases = suscripcionRepository
            .findByCliente_IdClienteAndEstado(idCliente, Suscripcion.Estado.ACTIVA).stream()
            .anyMatch(s -> s.isVigente() && s.getPlan().isIncluyeClases());
        if (!planConClases) {
            throw new NegocioException("Para inscribirte en clases necesitas un plan activo que las incluya");
        }
        inscribirEnClase(idCliente, idClase);
    }

    @Override
    public void cancelarInscripcion(Integer idCliente, Integer idClase) {
        Cliente cliente = buscar(idCliente);
        boolean quitada = cliente.getClases() != null
            && cliente.getClases().removeIf(c -> c.getIdClase().equals(idClase));
        if (!quitada) {
            throw new NegocioException("No estás inscrito en esa clase");
        }
    }

    @Override
    public void eliminar(Integer id) {
        Cliente cliente = buscar(id);
        // Borra primero lo que depende del cliente para no violar las claves foráneas
        rutinaRepository.deleteAll(rutinaRepository.findByCliente_IdCliente(id));
        alimentacionRepository.deleteAll(alimentacionRepository.findByCliente_IdCliente(id));
        suscripcionRepository.deleteByCliente_IdCliente(id);
        registroPesoRepository.deleteByCliente_IdCliente(id);
        // Un enlace de recuperación pendiente no debe servir si el id se reutilizara
        tokenRepository.findByTipoUsuarioAndIdUsuarioAndUsadoFalse(TokenRecuperacion.TipoUsuario.CLIENTE, id)
            .forEach(t -> t.setUsado(true));
        if (cliente.getClases() != null) {
            cliente.getClases().clear();
        }
        clienteRepository.delete(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> obtenerPorIdConClases(Integer id) {
        return clienteRepository.findByIdWithClases(id);
    }

    private Cliente buscar(Integer id) {
        return clienteRepository.findById(id)
            .orElseThrow(() -> new NegocioException("El cliente no existe"));
    }

    /** Documento y email identifican al usuario en el login, así que no pueden repetirse. */
    private void validarUnicos(ClienteDTO datos, Integer idActual) {
        clienteRepository.findByDocumento(datos.getDocumento())
            .filter(otro -> !Objects.equals(otro.getIdCliente(), idActual))
            .ifPresent(otro -> { throw new NegocioException("Ya existe un cliente con ese documento"); });
        if (datos.getEmail() != null) {
            validarEmailLibre(datos.getEmail(), idActual);
        }
    }

    private void validarEmailLibre(String email, Integer idActual) {
        clienteRepository.findByEmail(email)
            .filter(otro -> !Objects.equals(otro.getIdCliente(), idActual))
            .ifPresent(otro -> { throw new NegocioException("Ya existe una cuenta con ese email"); });
        if (administradorRepository.findByEmail(email).isPresent()
                || entrenadorRepository.findFirstByEmailAndPasswordIsNotNull(email).isPresent()) {
            throw new NegocioException("Ya existe una cuenta con ese email");
        }
    }
}
