package com.proyecto.fitpro.service;

import com.proyecto.fitpro.exception.NegocioException;
import com.proyecto.fitpro.model.Administrador;
import com.proyecto.fitpro.model.Cliente;
import com.proyecto.fitpro.model.Entrenador;
import com.proyecto.fitpro.model.TokenRecuperacion;
import com.proyecto.fitpro.model.TokenRecuperacion.TipoUsuario;
import com.proyecto.fitpro.repository.AdministradorRepository;
import com.proyecto.fitpro.repository.ClienteRepository;
import com.proyecto.fitpro.repository.EntrenadorRepository;
import com.proyecto.fitpro.repository.TokenRecuperacionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/** Recuperación de contraseña con enlaces de un solo uso que caducan a los 30 minutos. */
@Service
@Transactional
public class RecuperacionService {

    public static final int MINUTOS_VALIDEZ = 30;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TokenRecuperacionRepository tokenRepository;
    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificacionService notificacionService;
    private final String urlBase;

    public RecuperacionService(TokenRecuperacionRepository tokenRepository, ClienteRepository clienteRepository,
            AdministradorRepository administradorRepository, EntrenadorRepository entrenadorRepository,
            PasswordEncoder passwordEncoder, NotificacionService notificacionService,
            @Value("${fitpro.url-base:http://localhost:8082}") String urlBase) {
        this.tokenRepository = tokenRepository;
        this.clienteRepository = clienteRepository;
        this.administradorRepository = administradorRepository;
        this.entrenadorRepository = entrenadorRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificacionService = notificacionService;
        // La URL del enlace sale de la configuración, no de la petición: así nadie puede
        // manipular la cabecera Host para que el correo apunte a otro dominio
        this.urlBase = urlBase.replaceAll("/+$", "");
    }

    /**
     * Genera y envía el enlace. No indica si el email existe: la respuesta al usuario es siempre
     * la misma, para que el formulario no sirva para averiguar qué correos están registrados.
     */
    public void solicitar(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        buscarUsuario(email.trim()).ifPresent(usuario -> {
            // Un enlace nuevo invalida los anteriores que siguieran pendientes
            tokenRepository.findByTipoUsuarioAndIdUsuarioAndUsadoFalse(usuario.tipo(), usuario.id())
                .forEach(t -> t.setUsado(true));

            String token = generarToken();
            TokenRecuperacion registro = new TokenRecuperacion();
            registro.setTokenHash(hash(token));
            registro.setTipoUsuario(usuario.tipo());
            registro.setIdUsuario(usuario.id());
            registro.setExpira(LocalDateTime.now().plusMinutes(MINUTOS_VALIDEZ));
            tokenRepository.save(registro);

            notificacionService.enviar(usuario.email(), "FitPro - Restablece tu contraseña", """
                Hola %s,

                Recibimos una solicitud para restablecer tu contraseña de FitPro.
                Abre este enlace para elegir una nueva (vale %d minutos y un solo uso):

                %s/recuperar/%s

                Si no fuiste tú, ignora este correo: tu contraseña no cambiará.
                """.formatted(usuario.nombre(), MINUTOS_VALIDEZ, urlBase, token));
        });
    }

    @Transactional(readOnly = true)
    public boolean esValido(String token) {
        return buscarToken(token).isPresent();
    }

    public void restablecer(String token, String nuevaPassword) {
        TokenRecuperacion registro = buscarToken(token)
            .orElseThrow(() -> new NegocioException("El enlace no es válido o ya caducó. Solicita uno nuevo."));
        String hash = passwordEncoder.encode(nuevaPassword);
        switch (registro.getTipoUsuario()) {
            case CLIENTE -> clienteRepository.findById(registro.getIdUsuario()).ifPresent(c -> c.setPassword(hash));
            case ADMIN -> administradorRepository.findById(registro.getIdUsuario()).ifPresent(a -> a.setPassword(hash));
            case ENTRENADOR -> entrenadorRepository.findById(registro.getIdUsuario()).ifPresent(e -> e.setPassword(hash));
        }
        registro.setUsado(true);
    }

    private Optional<TokenRecuperacion> buscarToken(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return tokenRepository.findByTokenHash(hash(token)).filter(TokenRecuperacion::isValido);
    }

    private record Usuario(TipoUsuario tipo, Integer id, String email, String nombre) {}

    /** Mismo orden que el login: la cuenta que se recupera es la misma con la que se entra. */
    private Optional<Usuario> buscarUsuario(String email) {
        Optional<Cliente> cliente = clienteRepository.findByEmail(email);
        if (cliente.isPresent() && cliente.get().getPassword() != null) {
            return cliente.map(c -> new Usuario(TipoUsuario.CLIENTE, c.getIdCliente(), c.getEmail(), c.getNombre()));
        }
        Optional<Administrador> admin = administradorRepository.findByEmail(email);
        if (admin.isPresent()) {
            return admin.map(a -> new Usuario(TipoUsuario.ADMIN, a.getIdAdministrador(), a.getEmail(),
                a.getNombre() != null ? a.getNombre() : "administrador"));
        }
        Optional<Entrenador> entrenador = entrenadorRepository.findFirstByEmailAndPasswordIsNotNull(email);
        if (entrenador.isPresent()) {
            return entrenador.map(e -> new Usuario(TipoUsuario.ENTRENADOR, e.getIdEntrenador(), e.getEmail(), e.getNombre()));
        }
        // Cliente creado por un admin sin contraseña: puede ponerse una por esta vía
        return cliente.map(c -> new Usuario(TipoUsuario.CLIENTE, c.getIdCliente(), c.getEmail(), c.getNombre()));
    }

    private static String generarToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
