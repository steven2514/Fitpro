package com.proyecto.fitpro.config;

import com.proyecto.fitpro.model.Administrador;
import com.proyecto.fitpro.repository.AdministradorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Como el registro de administradores ya no es público, en una base de datos vacía
 * se crea el primer administrador a partir de variables de entorno.
 */
@Configuration
public class AdminInicialConfig {

    private static final Logger log = LoggerFactory.getLogger(AdminInicialConfig.class);

    @Bean
    public ApplicationRunner crearAdminInicial(AdministradorRepository administradorRepository,
            PasswordEncoder passwordEncoder,
            @Value("${fitpro.admin.email:}") String email,
            @Value("${fitpro.admin.password:}") String password) {
        return args -> {
            if (administradorRepository.count() > 0) {
                return;
            }
            if (email.isBlank() || password.isBlank()) {
                log.warn("No hay administradores. Define ADMIN_EMAIL y ADMIN_PASSWORD para crear el primero.");
                return;
            }
            Administrador admin = new Administrador();
            admin.setNombre("Administrador");
            admin.setApellido("FitPro");
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(password));
            administradorRepository.save(admin);
            log.info("Administrador inicial creado: {}", email);
        };
    }
}
