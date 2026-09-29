package com.proyecto.fitpro.controller;

import com.proyecto.fitpro.dto.CambioPasswordDTO;
import com.proyecto.fitpro.exception.NegocioException;
import com.proyecto.fitpro.service.RecuperacionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/recuperar")
public class RecuperacionController {

    private static final Logger log = LoggerFactory.getLogger(RecuperacionController.class);

    private final RecuperacionService recuperacionService;

    public RecuperacionController(RecuperacionService recuperacionService) {
        this.recuperacionService = recuperacionService;
    }

    @GetMapping
    public String formulario() {
        return "recuperar";
    }

    @PostMapping
    public String solicitar(@RequestParam(required = false) String email, Model model) {
        try {
            recuperacionService.solicitar(email);
        } catch (Exception e) {
            // Aunque falle, la respuesta es la misma: no se revela si el email existe
            log.error("Error al solicitar la recuperación de contraseña", e);
        }
        model.addAttribute("enviado", true);
        return "recuperar";
    }

    @GetMapping("/{token}")
    public String formularioNueva(@PathVariable String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("valido", recuperacionService.esValido(token));
        model.addAttribute("datos", new CambioPasswordDTO());
        return "recuperar-nueva";
    }

    @PostMapping("/{token}")
    public String restablecer(@PathVariable String token, @Valid @ModelAttribute("datos") CambioPasswordDTO datos,
            BindingResult result, Model model, RedirectAttributes ra) {
        model.addAttribute("token", token);
        model.addAttribute("valido", true);
        if (result.hasErrors()) {
            model.addAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
            return "recuperar-nueva";
        }
        try {
            recuperacionService.restablecer(token, datos.getNueva());
            ra.addFlashAttribute("mensaje", "Contraseña actualizada. Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (NegocioException e) {
            model.addAttribute("valido", false);
            return "recuperar-nueva";
        }
    }
}
