package com.proyecto.fitpro.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneral(Exception ex) {
        log.error("Error no controlado: ", ex);
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("error", "Error interno del servidor");
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return mav;
    }
}
