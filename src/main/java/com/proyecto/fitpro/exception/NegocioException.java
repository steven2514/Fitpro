package com.proyecto.fitpro.exception;

/**
 * Error de una regla de negocio. Su mensaje está pensado para mostrarse al usuario.
 */
public class NegocioException extends RuntimeException {

    public NegocioException(String message) {
        super(message);
    }
}
