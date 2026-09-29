package com.proyecto.fitpro.controller;

import com.proyecto.fitpro.dto.EjercicioDTO;
import com.proyecto.fitpro.dto.HorarioSemanal;
import com.proyecto.fitpro.exception.NegocioException;
import com.proyecto.fitpro.model.Entrenador;
import com.proyecto.fitpro.model.Rutina;
import com.proyecto.fitpro.service.EntrenadorService;
import com.proyecto.fitpro.service.RutinaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.function.Supplier;

/** Panel del entrenador: sus clases y horario, sus alumnos y las rutinas que les diseña. */
@Controller
@RequestMapping("/entrenador")
public class EntrenadorController {

    private static final Logger log = LoggerFactory.getLogger(EntrenadorController.class);
    private static final String PANEL = "redirect:/entrenador/panel";

    private final EntrenadorService entrenadorService;
    private final RutinaService rutinaService;

    public EntrenadorController(EntrenadorService entrenadorService, RutinaService rutinaService) {
        this.entrenadorService = entrenadorService;
        this.rutinaService = rutinaService;
    }

    @GetMapping("/panel")
    public String panel(Principal principal, Model model) {
        Integer id = idDe(principal);
        Entrenador entrenador = entrenadorService.obtenerPorId(id).orElse(null);
        if (entrenador == null) return "redirect:/login";
        var clases = entrenadorService.obtenerClases(id);
        model.addAttribute("entrenador", entrenador);
        model.addAttribute("clases", clases);
        model.addAttribute("horario", HorarioSemanal.de(clases));
        model.addAttribute("alumnos", entrenadorService.obtenerAlumnos(id));
        model.addAttribute("rutinas", entrenadorService.obtenerRutinas(id));
        return "entrenador-panel";
    }

    @PostMapping("/rutina/crear")
    public String crearRutina(@RequestParam Integer idCliente, @RequestParam String nombre,
            @RequestParam String objetivo, @RequestParam String nivel, Principal principal, RedirectAttributes ra) {
        return ejecutar(ra, "crear la rutina", PANEL + "#rutinas", () -> {
            Rutina rutina = entrenadorService.crearRutina(idDe(principal), idCliente, nombre, objetivo, nivel);
            ra.addFlashAttribute("success", "Rutina creada. Agrégale los ejercicios.");
            return "redirect:/entrenador/rutina/" + rutina.getIdRutina();
        });
    }

    @GetMapping("/rutina/{id}")
    public String verRutina(@PathVariable Integer id, Principal principal, Model model, RedirectAttributes ra) {
        try {
            Rutina rutina = entrenadorService.obtenerRutinaPropia(idDe(principal), id);
            model.addAttribute("rutina", rutina);
            model.addAttribute("ejercicios", rutinaService.obtenerEjercicios(id));
            if (!model.containsAttribute("ejercicio")) {
                model.addAttribute("ejercicio", new EjercicioDTO());
            }
            return "entrenador-rutina";
        } catch (NegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return PANEL;
        }
    }

    @PostMapping("/rutina/{id}/ejercicio")
    public String agregarEjercicio(@PathVariable Integer id, @Valid @ModelAttribute("ejercicio") EjercicioDTO ejercicio,
            BindingResult result, Principal principal, RedirectAttributes ra) {
        String destino = "redirect:/entrenador/rutina/" + id;
        if (result.hasErrors()) {
            ra.addFlashAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
            ra.addFlashAttribute("ejercicio", ejercicio);
            return destino;
        }
        return ejecutar(ra, "agregar el ejercicio", destino, () -> {
            entrenadorService.obtenerRutinaPropia(idDe(principal), id);
            rutinaService.agregarEjercicio(id, ejercicio);
            ra.addFlashAttribute("success", "Ejercicio agregado");
            return destino;
        });
    }

    @PostMapping("/rutina/{idRutina}/ejercicio/{idEjercicio}/eliminar")
    public String eliminarEjercicio(@PathVariable Integer idRutina, @PathVariable Integer idEjercicio,
            Principal principal, RedirectAttributes ra) {
        String destino = "redirect:/entrenador/rutina/" + idRutina;
        return ejecutar(ra, "eliminar el ejercicio", destino, () -> {
            Rutina rutina = entrenadorService.obtenerRutinaPropia(idDe(principal), idRutina);
            boolean esDeLaRutina = rutinaService.obtenerEjercicios(rutina.getIdRutina()).stream()
                .anyMatch(e -> e.getIdEjercicio().equals(idEjercicio));
            if (!esDeLaRutina) throw new NegocioException("El ejercicio no pertenece a esta rutina");
            rutinaService.eliminarEjercicio(idEjercicio);
            ra.addFlashAttribute("success", "Ejercicio eliminado");
            return destino;
        });
    }

    private Integer idDe(Principal principal) {
        return Integer.parseInt(principal.getName());
    }

    private String ejecutar(RedirectAttributes ra, String accion, String destinoError, Supplier<String> operacion) {
        try {
            return operacion.get();
        } catch (NegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error al {}", accion, e);
            ra.addFlashAttribute("error", "No se pudo " + accion + ". Intenta de nuevo.");
        }
        return destinoError;
    }
}
