package com.proyecto.fitpro.controller;

import com.proyecto.fitpro.dto.HorarioSemanal;
import com.proyecto.fitpro.model.Clase;
import com.proyecto.fitpro.model.Entrenador;
import com.proyecto.fitpro.repository.ClaseRepository;
import com.proyecto.fitpro.repository.EntrenadorRepository;
import com.proyecto.fitpro.service.ClienteService;
import com.proyecto.fitpro.service.PlanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
public class HomeController {

    private static final int ENTRENADORES_EN_PORTADA = 4;

    private final ClienteService clienteService;
    private final PlanService planService;
    private final ClaseRepository claseRepository;
    private final EntrenadorRepository entrenadorRepository;

    public HomeController(ClienteService clienteService, PlanService planService,
            ClaseRepository claseRepository, EntrenadorRepository entrenadorRepository) {
        this.clienteService = clienteService;
        this.planService = planService;
        this.claseRepository = claseRepository;
        this.entrenadorRepository = entrenadorRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Clase> clases = claseRepository.findAll();
        // Cifras reales de la base de datos en lugar de números de marketing inventados
        model.addAttribute("totalMiembros", clienteService.contar());
        model.addAttribute("totalClases", clases.size());
        model.addAttribute("totalEntrenadores", entrenadorRepository.count());
        model.addAttribute("planes", planService.obtenerActivos());
        model.addAttribute("horario", HorarioSemanal.de(clases));
        model.addAttribute("entrenadores", entrenadoresDestacados(clases));
        return "index";
    }

    /** Los entrenadores con más clases programadas, junto con cuántas imparten. */
    private List<Map.Entry<Entrenador, Long>> entrenadoresDestacados(List<Clase> clases) {
        Map<Integer, Entrenador> porId = new LinkedHashMap<>();
        Map<Integer, Long> conteo = new LinkedHashMap<>();
        clases.stream().map(Clase::getEntrenador).filter(Objects::nonNull).forEach(e -> {
            porId.putIfAbsent(e.getIdEntrenador(), e);
            conteo.merge(e.getIdEntrenador(), 1L, Long::sum);
        });
        return conteo.entrySet().stream()
            .sorted(Map.Entry.<Integer, Long>comparingByValue(Comparator.reverseOrder()))
            .limit(ENTRENADORES_EN_PORTADA)
            .map(e -> Map.entry(porId.get(e.getKey()), e.getValue()))
            .toList();
    }
}
