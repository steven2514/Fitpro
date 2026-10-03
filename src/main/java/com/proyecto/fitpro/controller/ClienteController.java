package com.proyecto.fitpro.controller;

import com.proyecto.fitpro.dto.CambioPasswordDTO;
import com.proyecto.fitpro.dto.DatosFisicosDTO;
import com.proyecto.fitpro.dto.GraficoPeso;
import com.proyecto.fitpro.dto.HorarioSemanal;
import com.proyecto.fitpro.dto.PerfilDTO;
import com.proyecto.fitpro.exception.NegocioException;
import com.proyecto.fitpro.model.*;
import com.proyecto.fitpro.service.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    private static final Logger log = LoggerFactory.getLogger(ClienteController.class);
    private static final String PANEL = "redirect:/cliente/panel";

    private final ClienteService clienteService;
    private final RutinaService rutinaService;
    private final AlimentacionService alimentacionService;
    private final ClaseService claseService;
    private final PlanService planService;
    private final SuscripcionService suscripcionService;
    private final ProgresoService progresoService;

    public ClienteController(ClienteService clienteService, RutinaService rutinaService,
            AlimentacionService alimentacionService, ClaseService claseService, PlanService planService,
            SuscripcionService suscripcionService, ProgresoService progresoService) {
        this.clienteService = clienteService;
        this.rutinaService = rutinaService;
        this.alimentacionService = alimentacionService;
        this.claseService = claseService;
        this.planService = planService;
        this.suscripcionService = suscripcionService;
        this.progresoService = progresoService;
    }

    @GetMapping("/panel")
    public String panel(Principal principal, Model model) {
        Integer idCliente = idDe(principal);
        Cliente cliente = clienteService.obtenerPorIdConClases(idCliente).orElse(null);
        if (cliente == null) return "redirect:/login";

        List<Clase> misClases = List.copyOf(cliente.getClases());
        Set<Integer> idsMisClases = new HashSet<>();
        misClases.forEach(c -> idsMisClases.add(c.getIdClase()));

        model.addAttribute("cliente", cliente);
        model.addAttribute("rutinas", rutinaService.obtenerPorCliente(idCliente));
        model.addAttribute("alimentaciones", alimentacionService.obtenerPorCliente(idCliente));
        model.addAttribute("clases", misClases.stream().sorted(HorarioSemanal.POR_HORARIO).toList());
        model.addAttribute("miSemana", HorarioSemanal.de(misClases));
        model.addAttribute("clasesDisponibles", claseService.obtenerTodas().stream()
            .filter(c -> !idsMisClases.contains(c.getIdClase()))
            .sorted(HorarioSemanal.POR_HORARIO).toList());

        // Se agrupa por id: los entrenadores llegan como proxies lazy y su equals() no es fiable
        Map<Integer, Entrenador> entrenadores = new LinkedHashMap<>();
        misClases.stream().map(Clase::getEntrenador).filter(Objects::nonNull)
            .forEach(e -> entrenadores.putIfAbsent(e.getIdEntrenador(), e));
        model.addAttribute("entrenadores", List.copyOf(entrenadores.values()));

        Optional<Suscripcion> suscripcion = suscripcionService.obtenerVigente(idCliente);
        model.addAttribute("suscripcion", suscripcion.orElse(null));
        model.addAttribute("puedeInscribirse", suscripcion.map(s -> s.getPlan().isIncluyeClases()).orElse(false));
        model.addAttribute("planes", planService.obtenerActivos());
        if (suscripcion.isEmpty()) {
            // Plan que terminó por fecha (no cancelado): se le avisa para que renueve
            suscripcionService.obtenerHistorial(idCliente).stream()
                .filter(s -> s.getEstado() == Suscripcion.Estado.ACTIVA && !s.isVigente())
                .findFirst()
                .ifPresent(vencida -> model.addAttribute("planVencido", vencida));
        }

        List<RegistroPeso> historial = progresoService.obtenerHistorial(idCliente);
        List<RegistroPeso> recientes = new ArrayList<>(historial.subList(Math.max(0, historial.size() - 8), historial.size()));
        Collections.reverse(recientes);
        model.addAttribute("historialPeso", recientes);
        model.addAttribute("grafico", GraficoPeso.desde(historial));
        return "cliente-panel";
    }

    @PostMapping("/actualizar-datos-fisicos")
    public String actualizarDatosFisicos(@Valid @ModelAttribute DatosFisicosDTO datos, BindingResult result,
            Principal principal, RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
            return PANEL + "#datos";
        }
        return ejecutar(ra, "Datos físicos actualizados y guardados en tu progreso", "actualizar tus datos", "#datos",
            () -> clienteService.actualizarDatosFisicos(idDe(principal), datos));
    }

    // ========== MI PERFIL ==========
    @GetMapping("/perfil")
    public String perfil(Principal principal, Model model) {
        Cliente cliente = clienteService.obtenerPorId(idDe(principal)).orElse(null);
        if (cliente == null) return "redirect:/login";
        model.addAttribute("cliente", cliente);
        if (!model.containsAttribute("perfil")) {
            model.addAttribute("perfil", PerfilDTO.fromEntity(cliente));
        }
        model.addAttribute("clave", new CambioPasswordDTO());
        model.addAttribute("historialSuscripciones", suscripcionService.obtenerHistorial(idDe(principal)));
        return "cliente-perfil";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(@Valid @ModelAttribute("perfil") PerfilDTO perfil, BindingResult result,
            Principal principal, RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
            ra.addFlashAttribute("perfil", perfil);
            return "redirect:/cliente/perfil";
        }
        ejecutar(ra, "Tus datos de contacto se actualizaron", "actualizar tu perfil", "",
            () -> clienteService.actualizarPerfil(idDe(principal), perfil));
        return "redirect:/cliente/perfil";
    }

    @PostMapping("/perfil/password")
    public String cambiarPassword(@Valid @ModelAttribute("clave") CambioPasswordDTO clave, BindingResult result,
            Principal principal, RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/cliente/perfil#seguridad";
        }
        ejecutar(ra, "Tu contraseña se cambió correctamente", "cambiar tu contraseña", "",
            () -> clienteService.cambiarPassword(idDe(principal), clave.getActual(), clave.getNueva()));
        return "redirect:/cliente/perfil#seguridad";
    }

    // ========== SUSCRIPCIÓN ==========
    @PostMapping("/suscripcion")
    public String suscribirse(@RequestParam Integer idPlan, Principal principal, RedirectAttributes ra) {
        return ejecutarConMensaje(ra, "suscribirte al plan", "#plan", mensaje -> {
            Suscripcion s = suscripcionService.suscribir(idDe(principal), idPlan);
            mensaje.accept("¡Listo! Tu plan " + s.getPlan().getNombre() + " está activo hasta el "
                + s.getFechaFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        });
    }

    @PostMapping("/suscripcion/cancelar")
    public String cancelarSuscripcion(Principal principal, RedirectAttributes ra) {
        return ejecutar(ra, "Tu suscripción fue cancelada", "cancelar tu suscripción", "#plan",
            () -> suscripcionService.cancelarVigente(idDe(principal)));
    }

    // ========== CLASES ==========
    @PostMapping("/clase/{idClase}/inscribir")
    public String inscribirse(@PathVariable Integer idClase, Principal principal, RedirectAttributes ra) {
        return ejecutar(ra, "Te inscribiste en la clase", "inscribirte en la clase", "#clases",
            () -> clienteService.inscribirsePorCuenta(idDe(principal), idClase));
    }

    @PostMapping("/clase/{idClase}/cancelar")
    public String cancelarClase(@PathVariable Integer idClase, Principal principal, RedirectAttributes ra) {
        return ejecutar(ra, "Cancelaste tu inscripción", "cancelar la inscripción", "#clases",
            () -> clienteService.cancelarInscripcion(idDe(principal), idClase));
    }

    // ========== AUXILIARES ==========

    private Integer idDe(Principal principal) {
        return Integer.parseInt(principal.getName());
    }

    private String ejecutar(RedirectAttributes ra, String exito, String accion, String ancla, Runnable operacion) {
        return ejecutarConMensaje(ra, accion, ancla, mensaje -> {
            operacion.run();
            mensaje.accept(exito);
        });
    }

    /** Igual que en el panel de admin: errores de negocio visibles, errores técnicos al log. */
    private String ejecutarConMensaje(RedirectAttributes ra, String accion, String ancla,
            Consumer<Consumer<String>> operacion) {
        try {
            operacion.accept(m -> ra.addFlashAttribute("success", m));
        } catch (NegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error al {}", accion, e);
            ra.addFlashAttribute("error", "No se pudo " + accion + ". Intenta de nuevo.");
        }
        return PANEL + ancla;
    }
}
