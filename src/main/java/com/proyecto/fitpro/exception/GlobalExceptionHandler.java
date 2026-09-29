package com.proyecto.fitpro.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import jakarta.persistence.EntityNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({EntityNotFoundException.class, NoResourceFoundException.class})
    public ModelAndView handleNotFound(Exception ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return vistaError("Recurso no encontrado", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ModelAndView handleValidationError(MethodArgumentNotValidException ex) {
        log.warn("Error de validación: {}", ex.getBindingResult().getFieldErrors());
        ModelAndView mav = vistaError("Datos inválidos", HttpStatus.BAD_REQUEST);
        mav.addObject("details", ex.getBindingResult().getFieldErrors());
        return mav;
    }

    // Parámetros que faltan o con formato incorrecto (p. ej. letras en un campo numérico)
    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ModelAndView handleBadRequest(Exception ex) {
        log.warn("Petición inválida: {}", ex.getMessage());
        return vistaError("Los datos enviados no son válidos. Revisa el formulario.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NegocioException.class)
    public ModelAndView handleNegocio(NegocioException ex) {
        return vistaError(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneral(Exception ex) {
        log.error("Error no controlado en: ", ex);
        return vistaError("Error interno del servidor. Por favor intenta más tarde.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ModelAndView vistaError(String mensaje, HttpStatus status) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("error", mensaje);
        mav.addObject("statusCode", status.value());
        mav.setStatus(status);
        return mav;
    }
}
