package com.proyecto.fitpro.config;

import com.proyecto.fitpro.model.Administrador;
import com.proyecto.fitpro.model.Cliente;
import com.proyecto.fitpro.repository.AdministradorRepository;
import com.proyecto.fitpro.repository.ClienteRepository;
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
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;

    public SecurityConfig(ClienteRepository clienteRepository, AdministradorRepository administradorRepository) {
        this.clienteRepository = clienteRepository;
        this.administradorRepository = administradorRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/registro", "/registroAdmin", "/css/**", "/js/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/cliente/**").hasRole("CLIENTE")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {
                    String role = authentication.getAuthorities().iterator().next().getAuthority();
                    if (role.equals("ROLE_ADMIN")) {
                        response.sendRedirect("/admin/panel");
                    } else {
                        response.sendRedirect("/cliente/panel");
                    }
                })
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Cliente cliente = clienteRepository.findByDocumento(username).orElse(null);
            if (cliente != null && cliente.getPassword() != null) {
                return new User(
                    String.valueOf(cliente.getIdCliente()),
                    cliente.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
                );
            }

            Cliente clienteEmail = clienteRepository.findByEmail(username).orElse(null);
            if (clienteEmail != null && clienteEmail.getPassword() != null) {
                return new User(
                    String.valueOf(clienteEmail.getIdCliente()),
                    clienteEmail.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
                );
            }

            Administrador admin = administradorRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
            return new User(
                String.valueOf(admin.getIdAdministrador()),
                admin.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
