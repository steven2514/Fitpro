package com.proyecto.fitpro.config;

import com.proyecto.fitpro.model.Cliente;
import com.proyecto.fitpro.repository.AdministradorRepository;
import com.proyecto.fitpro.repository.ClienteRepository;
import com.proyecto.fitpro.repository.EntrenadorRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Inicio de sesión y registro con Google. La cuenta se busca por el email que Google confirma,
 * en el mismo orden que el login con contraseña (cliente, administrador, entrenador con acceso).
 * Si no existe ninguna, se crea un cliente nuevo con el nombre de su cuenta de Google.
 */
@Service
public class GoogleLoginService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    /** Cuenta de FitPro a la que entra el usuario: su id y su rol. */
    public record Cuenta(int id, String rol) {}

    private final OidcUserService google = new OidcUserService();
    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;
    private final EntrenadorRepository entrenadorRepository;

    public GoogleLoginService(ClienteRepository clienteRepository, AdministradorRepository administradorRepository,
            EntrenadorRepository entrenadorRepository) {
        this.clienteRepository = clienteRepository;
        this.administradorRepository = administradorRepository;
        this.entrenadorRepository = entrenadorRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest request) {
        OidcUser usuario = google.loadUser(request);
        // Sin email verificado cualquiera podría entrar en la cuenta de otro poniendo su correo
        if (usuario.getEmail() == null || !Boolean.TRUE.equals(usuario.getEmailVerified())) {
            throw new OAuth2AuthenticationException(new OAuth2Error("email_no_verificado"),
                "La cuenta de Google no tiene un email verificado");
        }
        Cuenta cuenta = cuentaPara(usuario.getEmail(), usuario.getGivenName(), usuario.getFamilyName());
        return new UsuarioGoogle(cuenta, usuario);
    }

    @Transactional
    public Cuenta cuentaPara(String email, String nombre, String apellido) {
        var cliente = clienteRepository.findByEmail(email);
        if (cliente.isPresent()) {
            return new Cuenta(cliente.get().getIdCliente(), "ROLE_CLIENTE");
        }
        var admin = administradorRepository.findByEmail(email);
        if (admin.isPresent()) {
            return new Cuenta(admin.get().getIdAdministrador(), "ROLE_ADMIN");
        }
        var entrenador = entrenadorRepository.findFirstByEmailAndPasswordIsNotNull(email);
        if (entrenador.isPresent()) {
            return new Cuenta(entrenador.get().getIdEntrenador(), "ROLE_ENTRENADOR");
        }

        Cliente nuevo = new Cliente();
        nuevo.setEmail(email);
        nuevo.setNombre(texto(nombre, email.substring(0, email.indexOf('@'))));
        nuevo.setApellido(texto(apellido, "(sin apellido)"));
        return new Cuenta(clienteRepository.save(nuevo).getIdCliente(), "ROLE_CLIENTE");
    }

    /** Ajusta el valor a las reglas del nombre y apellido (2 a 50 caracteres). */
    private static String texto(String valor, String porDefecto) {
        String t = (valor == null || valor.isBlank()) ? porDefecto : valor.trim();
        if (t.length() < 2) t = porDefecto;
        return t.length() > 50 ? t.substring(0, 50) : t;
    }

    /** Usuario de Google cuyo nombre es el id de FitPro, igual que en el login con contraseña. */
    static class UsuarioGoogle extends DefaultOidcUser {
        private final String id;

        UsuarioGoogle(Cuenta cuenta, OidcUser google) {
            super(List.of(new SimpleGrantedAuthority(cuenta.rol())), google.getIdToken(), google.getUserInfo());
            this.id = String.valueOf(cuenta.id());
        }

        @Override
        public String getName() {
            return id;
        }
    }
}
