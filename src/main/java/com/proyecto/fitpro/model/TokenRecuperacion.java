package com.proyecto.fitpro.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Enlace de un solo uso para restablecer la contraseña. Sólo se guarda el hash SHA-256 del token:
 * si alguien lee la base de datos no puede usar los enlaces pendientes.
 */
@Entity
@Table(name = "token_recuperacion")
@Data
@NoArgsConstructor
public class TokenRecuperacion {

    public enum TipoUsuario { CLIENTE, ADMIN, ENTRENADOR }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idToken")
    private Integer idToken;

    @Column(name = "token_hash", length = 64, nullable = false, unique = true)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", length = 12, nullable = false)
    private TipoUsuario tipoUsuario;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "expira", nullable = false)
    private LocalDateTime expira;

    @Column(name = "usado", nullable = false)
    private boolean usado;

    public boolean isValido() {
        return !usado && LocalDateTime.now().isBefore(expira);
    }
}
