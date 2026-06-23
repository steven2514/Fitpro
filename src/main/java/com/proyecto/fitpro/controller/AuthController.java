package com.proyecto.fitpro.controller;

import com.proyecto.fitpro.model.Administrador;
import com.proyecto.fitpro.model.Cliente;
import com.proyecto.fitpro.repository.AdministradorRepository;
import com.proyecto.fitpro.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/registro")
    public String registroForm(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "registro";
    }

    @PostMapping("/registro")
    public String registro(@ModelAttribute Cliente cliente, Model model) {
        try {
            if (cliente.getPassword() != null && !cliente.getPassword().isBlank()) {
                cliente.setPassword(passwordEncoder.encode(cliente.getPassword()));
            }
            clienteRepository.save(cliente);
            model.addAttribute("mensaje", "Registro exitoso. Por favor inicia sesión.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Error al registrar: " + e.getMessage());
            return "registro";
        }
    }

    @GetMapping("/registroAdmin")
    public String registroAdmin(Model model) {
        model.addAttribute("administrador", new Administrador());
        return "registroAdmin";
    }

    @PostMapping("/registroAdmin")
    public String registroAdmin(@ModelAttribute Administrador administrador, Model model) {
        try {
            if (administrador.getPassword() != null && !administrador.getPassword().isBlank()) {
                administrador.setPassword(passwordEncoder.encode(administrador.getPassword()));
            }
            administradorRepository.save(administrador);
            model.addAttribute("mensaje", "Registro exitoso. Por favor inicia sesión.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Error al registrar: " + e.getMessage());
            return "registroAdmin";
        }
    }
}
