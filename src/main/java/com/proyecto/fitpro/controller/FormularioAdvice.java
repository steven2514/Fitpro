package com.proyecto.fitpro.controller;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice
public class FormularioAdvice {

    /**
     * Los campos de texto vacíos llegan como null: así un documento o email en blanco
     * no choca con las restricciones UNIQUE y una contraseña vacía significa "no cambiarla".
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
}
