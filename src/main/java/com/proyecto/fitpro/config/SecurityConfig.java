package com.proyecto.fitpro.config;

import com.proyecto.fitpro.model.Administrador;
import com.proyecto.fitpro.model.Cliente;
import com.proyecto.fitpro.model.Entrenador;
import com.proyecto.fitpro.repository.AdministradorRepository;
import com.proyecto.fitpro.repository.ClienteRepository;
import com.proyecto.fitpro.repository.EntrenadorRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Sin estilos ni scripts inline: todo sale de /css y /js. Sólo se permite Google Fonts como origen externo.
    private static final String CSP = "default-src 'self'; "
            + "style-src 'self' https://fonts.googleapis.com; "
            + "font-src 'self' https://fonts.gstatic.com; "
            + "script-src 'self'; "
            + "img-src 'self' data:; "
            + "frame-ancestors 'none'; form-action 'self'";

    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final IntentosLoginService intentosLogin;

    public SecurityConfig(ClienteRepository clienteRepository, AdministradorRepository administradorRepository,
            EntrenadorRepository entrenadorRepository, IntentosLoginService intentosLogin) {
        this.clienteRepository = clienteRepository;
        this.administradorRepository = administradorRepository;
        this.entrenadorRepository = entrenadorRepository;
        this.intentosLogin = intentosLogin;
    }

    /**
     * Corta el envío del login antes de comprobar la contraseña si la IP está bloqueada: así,
     * mientras dure el bloqueo, ni siquiera una contraseña correcta sirve para seguir probando.
     */
    private OncePerRequestFilter filtroLoginBloqueado() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                    FilterChain chain) throws ServletException, IOException {
                if ("POST".equals(request.getMethod()) && (request.getContextPath() + "/login").equals(request.getRequestURI())
                        && intentosLogin.estaBloqueada(request.getRemoteAddr())) {
                    response.sendRedirect(request.getContextPath() + "/login?bloqueado=true");
                    return;
                }
                chain.doFilter(request, response);
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/registro", "/recuperar", "/recuperar/**", "/error",
                    "/css/**", "/js/**", "/images/**", "/fonts/**").permitAll()
                // Sólo un administrador puede crear otros administradores
                .requestMatchers("/admin/**", "/registroAdmin").hasRole("ADMIN")
                .requestMatchers("/cliente/**").hasRole("CLIENTE")
                .requestMatchers("/entrenador/**").hasRole("ENTRENADOR")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {
                    intentosLogin.registrarExito(request.getRemoteAddr());
                    String rol = authentication.getAuthorities().iterator().next().getAuthority();
                    response.sendRedirect(switch (rol) {
                        case "ROLE_ADMIN" -> "/admin/panel";
                        case "ROLE_ENTRENADOR" -> "/entrenador/panel";
                        default -> "/cliente/panel";
                    });
                })
                // Cada fallo suma; al llegar al máximo, la IP queda bloqueada unos minutos.
                .failureHandler((request, response, exception) -> {
                    boolean bloqueada = intentosLogin.registrarFallo(request.getRemoteAddr());
                    response.sendRedirect(request.getContextPath()
                        + (bloqueada ? "/login?bloqueado=true" : "/login?error=true"));
                })
                .permitAll()
            )
            .addFilterBefore(filtroLoginBloqueado(), UsernamePasswordAuthenticationFilter.class)
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentSecurityPolicy(csp -> csp.policyDirectives(CSP))
            );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Optional<Cliente> cliente = clienteRepository.findByDocumento(username)
                .or(() -> clienteRepository.findByEmail(username))
                .filter(c -> c.getPassword() != null);
            if (cliente.isPresent()) {
                return new User(
                    String.valueOf(cliente.get().getIdCliente()),
                    cliente.get().getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
                );
            }

            Optional<Administrador> admin = administradorRepository.findByEmail(username);
            if (admin.isPresent()) {
                return new User(
                    String.valueOf(admin.get().getIdAdministrador()),
                    admin.get().getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );
            }

            Entrenador entrenador = entrenadorRepository.findFirstByEmailAndPasswordIsNotNull(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
            return new User(
                String.valueOf(entrenador.getIdEntrenador()),
                entrenador.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_ENTRENADOR"))
            );
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
