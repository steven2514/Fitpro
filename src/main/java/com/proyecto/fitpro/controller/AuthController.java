package com.proyecto.fitpro.controller;

import com.proyecto.fitpro.dto.AdministradorDTO;
import com.proyecto.fitpro.dto.ClienteDTO;
import com.proyecto.fitpro.exception.NegocioException;
import com.proyecto.fitpro.service.AdministradorService;
import com.proyecto.fitpro.service.ClienteService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final ClienteService clienteService;
    private final AdministradorService administradorService;

    public AuthController(ClienteService clienteService, AdministradorService administradorService) {
        this.clienteService = clienteService;
        this.administradorService = administradorService;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/registro")
    public String registroForm(Model model) {
        model.addAttribute("cliente", new ClienteDTO());
        return "registro";
    }

    @PostMapping("/registro")
    public String registro(@Valid @ModelAttribute("cliente") ClienteDTO cliente, BindingResult result,
            Model model, RedirectAttributes redirectAttributes) {
        if (cliente.getPassword() == null) {
            result.rejectValue("password", "requerido", "La contraseña es requerida");
        }
        if (result.hasErrors()) {
            return "registro";
        }
        try {
            clienteService.registrar(cliente);
            redirectAttributes.addFlashAttribute("mensaje", "Registro exitoso. Por favor inicia sesión.");
            return "redirect:/login";
        } catch (NegocioException e) {
            model.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error al registrar cliente", e);
            model.addAttribute("error", "No se pudo completar el registro. Intenta de nuevo.");
        }
        return "registro";
    }

    @GetMapping("/registroAdmin")
    public String registroAdminForm(Model model) {
        model.addAttribute("administrador", new AdministradorDTO());
        return "registroAdmin";
    }

    @PostMapping("/registroAdmin")
    public String registroAdmin(@Valid @ModelAttribute("administrador") AdministradorDTO administrador,
            BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "registroAdmin";
        }
        try {
            administradorService.registrar(administrador);
            redirectAttributes.addFlashAttribute("success", "Administrador creado exitosamente");
            return "redirect:/admin/panel";
        } catch (NegocioException e) {
            model.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error al registrar administrador", e);
            model.addAttribute("error", "No se pudo crear el administrador. Intenta de nuevo.");
        }
        return "registroAdmin";
    }
}
