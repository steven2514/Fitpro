package com.proyecto.fitpro.controller;

import com.proyecto.fitpro.model.*;
import com.proyecto.fitpro.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private RutinaService rutinaService;

    @Autowired
    private AlimentacionService alimentacionService;

    @GetMapping("/panel")
    public String panel(Principal principal, Model model) {
        Integer idCliente = Integer.parseInt(principal.getName());
        Cliente cliente = clienteService.obtenerPorIdConClases(idCliente).orElse(null);
        if (cliente == null) return "redirect:/login";

        model.addAttribute("cliente", cliente);
        model.addAttribute("rutinas", rutinaService.obtenerPorCliente(idCliente));
        model.addAttribute("alimentaciones", alimentacionService.obtenerPorCliente(idCliente));
        model.addAttribute("clases", cliente.getClases());
        model.addAttribute("entrenadores",
            cliente.getClases().stream().map(Clase::getEntrenador).distinct().collect(Collectors.toList()));
        return "cliente-panel";
    }

    @PostMapping("/actualizar-datos-fisicos")
    public String actualizarDatosFisicos(
            @RequestParam(required = false) Double peso,
            @RequestParam(required = false) Double altura,
            @RequestParam(required = false) Integer edad,
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) String objetivoFitness,
            @RequestParam(required = false) String nivelActividad,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            Integer idCliente = Integer.parseInt(principal.getName());
            Cliente cliente = clienteService.obtenerPorId(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            cliente.setPeso(peso);
            cliente.setAltura(altura);
            cliente.setEdad(edad);
            cliente.setGenero(genero);
            cliente.setObjetivoFitness(objetivoFitness);
            cliente.setNivelActividad(nivelActividad);
            clienteService.actualizar(cliente);
            redirectAttributes.addFlashAttribute("success", "Datos físicos actualizados correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar datos: " + e.getMessage());
        }
        return "redirect:/cliente/panel";
    }
}
