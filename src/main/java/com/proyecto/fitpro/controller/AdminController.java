package com.proyecto.fitpro.controller;

import com.proyecto.fitpro.dto.ClienteDTO;
import com.proyecto.fitpro.dto.EjercicioDTO;
import com.proyecto.fitpro.dto.HorarioSemanal;
import com.proyecto.fitpro.dto.PlanDTO;
import com.proyecto.fitpro.exception.NegocioException;
import com.proyecto.fitpro.model.*;
import com.proyecto.fitpro.service.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Panel de administración, dividido en secciones (resumen, clientes, suscripciones, rutinas,
 * alimentación, entrenadores, clases y reportes). Cada acción vuelve a la sección de su entidad.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private static final int CLIENTES_POR_PAGINA = 20;

    private static final String CLIENTES = "redirect:/admin/clientes";
    private static final String SUSCRIPCIONES = "redirect:/admin/suscripciones";
    private static final String RUTINAS = "redirect:/admin/rutinas";
    private static final String ALIMENTACION = "redirect:/admin/alimentacion";
    private static final String ENTRENADORES = "redirect:/admin/entrenadores";
    private static final String CLASES = "redirect:/admin/clases";

    private final ClienteService clienteService;
    private final RutinaService rutinaService;
    private final AlimentacionService alimentacionService;
    private final EntrenadorService entrenadorService;
    private final ClaseService claseService;
    private final AdministradorService administradorService;
    private final PlanService planService;
    private final SuscripcionService suscripcionService;
    private final ReporteService reporteService;

    public AdminController(ClienteService clienteService, RutinaService rutinaService,
            AlimentacionService alimentacionService, EntrenadorService entrenadorService,
            ClaseService claseService, AdministradorService administradorService,
            PlanService planService, SuscripcionService suscripcionService, ReporteService reporteService) {
        this.clienteService = clienteService;
        this.rutinaService = rutinaService;
        this.alimentacionService = alimentacionService;
        this.entrenadorService = entrenadorService;
        this.claseService = claseService;
        this.administradorService = administradorService;
        this.planService = planService;
        this.suscripcionService = suscripcionService;
        this.reporteService = reporteService;
    }

    // ================= PÁGINAS =================

    @GetMapping("/panel")
    public String resumen(@RequestParam(required = false) String documento, Principal principal, Model model) {
        base(principal, model, "resumen");
        List<Suscripcion> vigentes = suscripcionService.obtenerVigentes();
        List<Clase> clases = claseService.obtenerTodas();
        model.addAttribute("totalClientes", clienteService.contar());
        model.addAttribute("totalSuscripciones", vigentes.size());
        model.addAttribute("ingresosMensuales", ReporteService.pesos(suscripcionService.ingresosMensuales()));
        model.addAttribute("totalPlanesActivos", planService.obtenerActivos().size());
        model.addAttribute("totalRutinas", rutinaService.obtenerTodas().size());
        model.addAttribute("totalAlimentaciones", alimentacionService.obtenerTodas().size());
        model.addAttribute("totalEntrenadores", entrenadorService.obtenerTodos().size());
        model.addAttribute("totalClases", clases.size());
        model.addAttribute("porVencer", suscripcionService.obtenerPorVencer());
        model.addAttribute("ultimosClientes", clienteService.buscar(null, 0, 5).getContent());
        DayOfWeek hoy = LocalDate.now().getDayOfWeek();
        model.addAttribute("clasesHoy", clases.stream()
            .filter(c -> c.getDiaSemana() == hoy && c.getHora() != null)
            .sorted(HorarioSemanal.POR_HORARIO).toList());
        if (documento != null) {
            clienteService.obtenerPorDocumento(documento.trim()).ifPresent(cliente -> {
                model.addAttribute("clienteEncontrado", cliente);
                model.addAttribute("suscripcionEncontrado",
                    suscripcionService.obtenerVigente(cliente.getIdCliente()).orElse(null));
            });
            model.addAttribute("documentoBuscado", documento);
        }
        return "admin-panel";
    }

    /** Ruta antigua del buscador: se mantiene para no romper enlaces guardados. */
    @GetMapping("/cliente/buscar")
    public String buscarCliente(@RequestParam(required = false) String documento, Principal principal, Model model) {
        return resumen(documento, principal, model);
    }

    @GetMapping("/clientes")
    public String clientes(@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int pagina,
            Principal principal, Model model) {
        base(principal, model, "clientes");
        model.addAttribute("paginaClientes", clienteService.buscar(q, pagina, CLIENTES_POR_PAGINA));
        model.addAttribute("q", q);
        return "admin-clientes";
    }

    @GetMapping("/suscripciones")
    public String suscripciones(Principal principal, Model model) {
        base(principal, model, "suscripciones");
        List<Plan> planes = planService.obtenerTodos();
        Map<Integer, Long> suscriptoresPorPlan = new LinkedHashMap<>();
        planes.forEach(p -> suscriptoresPorPlan.put(p.getIdPlan(), suscripcionService.contarVigentesPorPlan(p.getIdPlan())));
        model.addAttribute("planes", planes);
        model.addAttribute("planesActivos", planes.stream().filter(Plan::isActivo).toList());
        model.addAttribute("suscriptoresPorPlan", suscriptoresPorPlan);
        model.addAttribute("suscripciones", suscripcionService.obtenerVigentes());
        model.addAttribute("clientes", clienteService.obtenerTodos());
        model.addAttribute("ingresosMensuales", ReporteService.pesos(suscripcionService.ingresosMensuales()));
        if (!model.containsAttribute("nuevoPlan")) {
            model.addAttribute("nuevoPlan", new PlanDTO());
        }
        return "admin-suscripciones";
    }

    @GetMapping("/rutinas")
    public String rutinas(Principal principal, Model model) {
        base(principal, model, "rutinas");
        model.addAttribute("rutinas", rutinaService.obtenerTodas());
        model.addAttribute("clientes", clienteService.obtenerTodos());
        return "admin-rutinas";
    }

    @GetMapping("/alimentacion")
    public String alimentacion(Principal principal, Model model) {
        base(principal, model, "alimentacion");
        model.addAttribute("alimentaciones", alimentacionService.obtenerTodas());
        model.addAttribute("clientes", clienteService.obtenerTodos());
        return "admin-alimentacion";
    }

    @GetMapping("/entrenadores")
    public String entrenadores(Principal principal, Model model) {
        base(principal, model, "entrenadores");
        model.addAttribute("entrenadores", entrenadorService.obtenerTodos());
        return "admin-entrenadores";
    }

    @GetMapping("/clases")
    public String clases(Principal principal, Model model) {
        base(principal, model, "clases");
        List<Clase> clases = claseService.obtenerTodas().stream().sorted(HorarioSemanal.POR_HORARIO).toList();
        model.addAttribute("clases", clases);
        model.addAttribute("horario", HorarioSemanal.de(clases));
        model.addAttribute("entrenadores", entrenadorService.obtenerTodos());
        model.addAttribute("clientes", clienteService.obtenerTodos());
        model.addAttribute("dias", HorarioSemanal.nombresDias());
        return "admin-clases";
    }

    @GetMapping("/reportes")
    public String reportes(Principal principal, Model model) {
        base(principal, model, "reportes");
        model.addAttribute("ingresosPorMes", reporteService.ingresosPorMes(6));
        model.addAttribute("clientesPorMes", reporteService.clientesNuevosPorMes(6));
        model.addAttribute("suscripcionesPorPlan", reporteService.suscripcionesPorPlan());
        model.addAttribute("ocupacionClases", reporteService.ocupacionClases(10));
        model.addAttribute("ingresosDelMes", ReporteService.pesos(reporteService.ingresosDelMes()));
        model.addAttribute("ingresosRecurrentes", ReporteService.pesos(suscripcionService.ingresosMensuales()));
        model.addAttribute("clientesNuevosDelMes", reporteService.clientesNuevosDelMes());
        model.addAttribute("ocupacionPromedio", reporteService.ocupacionPromedio());
        return "admin-reportes";
    }

    // ================= CLIENTES =================
    @PostMapping("/cliente/crear")
    public String crearCliente(@Valid @ModelAttribute ClienteDTO cliente, BindingResult result,
            RedirectAttributes ra) {
        if (tieneErrores(result, ra)) return CLIENTES;
        return ejecutar(ra, "Cliente creado exitosamente", "crear el cliente", CLIENTES,
            () -> clienteService.registrar(cliente));
    }

    @GetMapping("/cliente/editar/{id}")
    public String editarClienteForm(@PathVariable Integer id, Model model) {
        Optional<Cliente> cliente = clienteService.obtenerPorId(id);
        if (cliente.isEmpty()) return CLIENTES;
        model.addAttribute("idCliente", id);
        model.addAttribute("cliente", ClienteDTO.fromEntity(cliente.get()));
        model.addAttribute("historialSuscripciones", suscripcionService.obtenerHistorial(id));
        return "admin-cliente-editar";
    }

    @PostMapping("/cliente/editar/{id}")
    public String editarCliente(@PathVariable Integer id, @Valid @ModelAttribute ClienteDTO cliente,
            BindingResult result, RedirectAttributes ra) {
        if (tieneErrores(result, ra)) return "redirect:/admin/cliente/editar/" + id;
        return ejecutar(ra, "Cliente actualizado exitosamente", "actualizar el cliente", CLIENTES,
            () -> clienteService.actualizarCuenta(id, cliente));
    }

    @PostMapping("/cliente/eliminar/{id}")
    public String eliminarCliente(@PathVariable Integer id, RedirectAttributes ra) {
        return ejecutar(ra, "Cliente eliminado exitosamente", "eliminar el cliente", CLIENTES,
            () -> clienteService.eliminar(id));
    }

    // ================= RUTINAS Y EJERCICIOS =================
    @PostMapping("/rutina/crear")
    public String crearRutina(@RequestParam Integer idCliente, @RequestParam String nombre,
            @RequestParam String objetivo, @RequestParam String nivel, RedirectAttributes ra) {
        try {
            Rutina rutina = new Rutina();
            rutina.setCliente(buscarCliente(idCliente));
            rutina.setNombre(nombre);
            rutina.setObjetivo(objetivo);
            rutina.setNivel(nivel);
            Rutina creada = rutinaService.crear(rutina);
            ra.addFlashAttribute("success", "Rutina creada. Ahora agrégale sus ejercicios.");
            // Se lleva al admin directo a la rutina para que cargue los ejercicios
            return "redirect:/admin/rutina/editar/" + creada.getIdRutina();
        } catch (NegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error al crear la rutina", e);
            ra.addFlashAttribute("error", "No se pudo crear la rutina. Intenta de nuevo.");
        }
        return RUTINAS;
    }

    @GetMapping("/rutina/editar/{id}")
    public String editarRutinaForm(@PathVariable Integer id, Model model) {
        Optional<Rutina> rutina = rutinaService.obtenerPorId(id);
        if (rutina.isEmpty()) return RUTINAS;
        model.addAttribute("rutina", rutina.get());
        model.addAttribute("ejercicios", rutinaService.obtenerEjercicios(id));
        model.addAttribute("clientes", clienteService.obtenerTodos());
        if (!model.containsAttribute("ejercicio")) {
            model.addAttribute("ejercicio", new EjercicioDTO());
        }
        return "admin-rutina-editar";
    }

    @PostMapping("/rutina/editar/{id}")
    public String editarRutina(@PathVariable Integer id, @RequestParam Integer idCliente,
            @RequestParam String nombre, @RequestParam String objetivo, @RequestParam String nivel,
            RedirectAttributes ra) {
        return ejecutar(ra, "Rutina actualizada exitosamente", "actualizar la rutina",
            "redirect:/admin/rutina/editar/" + id, () -> {
                Rutina rutina = rutinaService.obtenerPorId(id)
                    .orElseThrow(() -> new NegocioException("La rutina no existe"));
                rutina.setCliente(buscarCliente(idCliente));
                rutina.setNombre(nombre);
                rutina.setObjetivo(objetivo);
                rutina.setNivel(nivel);
                rutinaService.actualizar(rutina);
            });
    }

    @PostMapping("/rutina/eliminar/{id}")
    public String eliminarRutina(@PathVariable Integer id, RedirectAttributes ra) {
        return ejecutar(ra, "Rutina eliminada exitosamente", "eliminar la rutina", RUTINAS,
            () -> rutinaService.eliminar(id));
    }

    @PostMapping("/rutina/{id}/ejercicio")
    public String agregarEjercicio(@PathVariable Integer id, @Valid @ModelAttribute("ejercicio") EjercicioDTO ejercicio,
            BindingResult result, RedirectAttributes ra) {
        String destino = "redirect:/admin/rutina/editar/" + id + "#ejercicios";
        if (tieneErrores(result, ra)) {
            ra.addFlashAttribute("ejercicio", ejercicio);
            return destino;
        }
        return ejecutar(ra, "Ejercicio agregado", "agregar el ejercicio", destino,
            () -> rutinaService.agregarEjercicio(id, ejercicio));
    }

    @PostMapping("/ejercicio/eliminar/{id}")
    public String eliminarEjercicio(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Integer idRutina = rutinaService.eliminarEjercicio(id);
            ra.addFlashAttribute("success", "Ejercicio eliminado");
            return "redirect:/admin/rutina/editar/" + idRutina + "#ejercicios";
        } catch (NegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error al eliminar el ejercicio", e);
            ra.addFlashAttribute("error", "No se pudo eliminar el ejercicio. Intenta de nuevo.");
        }
        return RUTINAS;
    }

    // ================= ALIMENTACIÓN =================
    @PostMapping("/alimentacion/crear")
    public String crearAlimentacion(@RequestParam Integer idCliente, @RequestParam String tipoComida,
            @RequestParam Integer calorias, @RequestParam String descripcion, RedirectAttributes ra) {
        return ejecutar(ra, "Plan alimenticio creado exitosamente", "crear el plan", ALIMENTACION, () -> {
            validarPositivo(calorias, "Las calorías");
            Alimentacion a = new Alimentacion();
            a.setCliente(buscarCliente(idCliente));
            a.setTipoComida(tipoComida);
            a.setCalorias(calorias);
            a.setDescripcion(descripcion);
            alimentacionService.crear(a);
        });
    }

    @GetMapping("/alimentacion/editar/{id}")
    public String editarAlimentacionForm(@PathVariable Integer id, Model model) {
        Optional<Alimentacion> a = alimentacionService.obtenerPorId(id);
        if (a.isEmpty()) return ALIMENTACION;
        model.addAttribute("alimentacion", a.get());
        model.addAttribute("clientes", clienteService.obtenerTodos());
        return "admin-alimentacion-editar";
    }

    @PostMapping("/alimentacion/editar/{id}")
    public String editarAlimentacion(@PathVariable Integer id, @RequestParam Integer idCliente,
            @RequestParam String tipoComida, @RequestParam Integer calorias,
            @RequestParam String descripcion, RedirectAttributes ra) {
        return ejecutar(ra, "Plan actualizado exitosamente", "actualizar el plan", ALIMENTACION, () -> {
            validarPositivo(calorias, "Las calorías");
            Alimentacion a = alimentacionService.obtenerPorId(id)
                .orElseThrow(() -> new NegocioException("El plan no existe"));
            a.setCliente(buscarCliente(idCliente));
            a.setTipoComida(tipoComida);
            a.setCalorias(calorias);
            a.setDescripcion(descripcion);
            alimentacionService.actualizar(a);
        });
    }

    @PostMapping("/alimentacion/eliminar/{id}")
    public String eliminarAlimentacion(@PathVariable Integer id, RedirectAttributes ra) {
        return ejecutar(ra, "Plan eliminado exitosamente", "eliminar el plan", ALIMENTACION,
            () -> alimentacionService.eliminar(id));
    }

    // ================= ENTRENADORES =================
    @PostMapping("/entrenador/crear")
    public String crearEntrenador(@ModelAttribute Entrenador entrenador, RedirectAttributes ra) {
        return ejecutar(ra, "Entrenador creado exitosamente", "crear el entrenador", ENTRENADORES,
            () -> entrenadorService.crear(entrenador));
    }

    @GetMapping("/entrenador/editar/{id}")
    public String editarEntrenadorForm(@PathVariable Integer id, Model model) {
        Optional<Entrenador> e = entrenadorService.obtenerPorId(id);
        if (e.isEmpty()) return ENTRENADORES;
        model.addAttribute("entrenador", e.get());
        model.addAttribute("clasesEntrenador", entrenadorService.obtenerClases(id));
        return "admin-entrenador-editar";
    }

    @PostMapping("/entrenador/editar/{id}")
    public String editarEntrenador(@PathVariable Integer id, @ModelAttribute Entrenador entrenador,
            RedirectAttributes ra) {
        return ejecutar(ra, "Entrenador actualizado exitosamente", "actualizar el entrenador", ENTRENADORES,
            () -> entrenadorService.actualizar(id, entrenador));
    }

    @PostMapping("/entrenador/{id}/acceso")
    public String darAccesoEntrenador(@PathVariable Integer id, @RequestParam String password, RedirectAttributes ra) {
        String destino = "redirect:/admin/entrenador/editar/" + id;
        if (password == null || password.length() < 8 || password.length() > 72) {
            ra.addFlashAttribute("error", "La contraseña debe tener entre 8 y 72 caracteres");
            return destino;
        }
        return ejecutar(ra, "Acceso guardado: el entrenador ya puede entrar con su email", "dar acceso al entrenador",
            destino, () -> entrenadorService.establecerAcceso(id, password));
    }

    @PostMapping("/entrenador/{id}/quitar-acceso")
    public String quitarAccesoEntrenador(@PathVariable Integer id, RedirectAttributes ra) {
        return ejecutar(ra, "Se quitó el acceso del entrenador", "quitar el acceso",
            "redirect:/admin/entrenador/editar/" + id, () -> entrenadorService.quitarAcceso(id));
    }

    @PostMapping("/entrenador/eliminar/{id}")
    public String eliminarEntrenador(@PathVariable Integer id, RedirectAttributes ra) {
        return ejecutar(ra, "Entrenador eliminado exitosamente", "eliminar el entrenador", ENTRENADORES,
            () -> entrenadorService.eliminar(id));
    }

    // ================= CLASES =================
    @PostMapping("/clase/crear")
    public String crearClase(@RequestParam String nombre, @RequestParam String descripcion,
            @RequestParam Integer capacidad, @RequestParam Integer idEntrenador,
            @RequestParam(required = false) DayOfWeek diaSemana,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime hora,
            @RequestParam(required = false) Integer duracionMinutos, RedirectAttributes ra) {
        return ejecutar(ra, "Clase creada exitosamente", "crear la clase", CLASES, () -> {
            validarPositivo(capacidad, "La capacidad");
            Clase clase = new Clase();
            clase.setNombre(nombre);
            clase.setDescripcion(descripcion);
            clase.setCapacidad(capacidad);
            clase.setEntrenador(buscarEntrenador(idEntrenador));
            aplicarHorario(clase, diaSemana, hora, duracionMinutos);
            claseService.crear(clase);
        });
    }

    @GetMapping("/clase/editar/{id}")
    public String editarClaseForm(@PathVariable Integer id, Model model) {
        Optional<Clase> c = claseService.obtenerPorId(id);
        if (c.isEmpty()) return CLASES;
        model.addAttribute("clase", c.get());
        model.addAttribute("entrenadores", entrenadorService.obtenerTodos());
        model.addAttribute("dias", HorarioSemanal.nombresDias());
        return "admin-clase-editar";
    }

    @PostMapping("/clase/editar/{id}")
    public String editarClase(@PathVariable Integer id, @RequestParam String nombre,
            @RequestParam String descripcion, @RequestParam Integer capacidad,
            @RequestParam Integer idEntrenador,
            @RequestParam(required = false) DayOfWeek diaSemana,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime hora,
            @RequestParam(required = false) Integer duracionMinutos, RedirectAttributes ra) {
        return ejecutar(ra, "Clase actualizada exitosamente", "actualizar la clase", CLASES, () -> {
            validarPositivo(capacidad, "La capacidad");
            Clase c = claseService.obtenerPorId(id)
                .orElseThrow(() -> new NegocioException("La clase no existe"));
            if (capacidad < c.getInscritos()) {
                throw new NegocioException("La capacidad no puede ser menor que los " + c.getInscritos() + " inscritos actuales");
            }
            c.setNombre(nombre);
            c.setDescripcion(descripcion);
            c.setCapacidad(capacidad);
            c.setEntrenador(buscarEntrenador(idEntrenador));
            aplicarHorario(c, diaSemana, hora, duracionMinutos);
            claseService.actualizar(c);
        });
    }

    @PostMapping("/clase/eliminar/{id}")
    public String eliminarClase(@PathVariable Integer id, RedirectAttributes ra) {
        return ejecutar(ra, "Clase eliminada exitosamente", "eliminar la clase", CLASES,
            () -> claseService.eliminar(id));
    }

    @PostMapping("/clase/inscribir")
    public String inscribirCliente(@RequestParam Integer idCliente, @RequestParam Integer idClase,
            RedirectAttributes ra) {
        return ejecutar(ra, "Cliente inscrito exitosamente", "inscribir al cliente", CLASES,
            () -> clienteService.inscribirEnClase(idCliente, idClase));
    }

    // ================= PLANES DE SUSCRIPCIÓN =================
    @PostMapping("/plan/crear")
    public String crearPlan(@Valid @ModelAttribute PlanDTO plan, BindingResult result, RedirectAttributes ra) {
        if (tieneErrores(result, ra)) return SUSCRIPCIONES;
        return ejecutar(ra, "Plan creado exitosamente", "crear el plan de suscripción", SUSCRIPCIONES,
            () -> planService.crear(plan));
    }

    @GetMapping("/plan/editar/{id}")
    public String editarPlanForm(@PathVariable Integer id, Model model) {
        Optional<Plan> plan = planService.obtenerPorId(id);
        if (plan.isEmpty()) return SUSCRIPCIONES;
        model.addAttribute("idPlan", id);
        model.addAttribute("plan", PlanDTO.fromEntity(plan.get()));
        return "admin-plan-editar";
    }

    @PostMapping("/plan/editar/{id}")
    public String editarPlan(@PathVariable Integer id, @Valid @ModelAttribute PlanDTO plan, BindingResult result,
            RedirectAttributes ra) {
        if (tieneErrores(result, ra)) return "redirect:/admin/plan/editar/" + id;
        return ejecutar(ra, "Plan actualizado exitosamente", "actualizar el plan de suscripción", SUSCRIPCIONES,
            () -> planService.actualizar(id, plan));
    }

    @PostMapping("/plan/estado/{id}")
    public String cambiarEstadoPlan(@PathVariable Integer id, RedirectAttributes ra) {
        return ejecutar(ra, "Estado del plan actualizado", "cambiar el estado del plan", SUSCRIPCIONES,
            () -> planService.cambiarEstado(id));
    }

    @PostMapping("/plan/eliminar/{id}")
    public String eliminarPlan(@PathVariable Integer id, RedirectAttributes ra) {
        return ejecutar(ra, "Plan eliminado exitosamente", "eliminar el plan de suscripción", SUSCRIPCIONES,
            () -> planService.eliminar(id));
    }

    @PostMapping("/suscripcion/asignar")
    public String asignarSuscripcion(@RequestParam Integer idCliente, @RequestParam Integer idPlan,
            RedirectAttributes ra) {
        return ejecutar(ra, "Plan asignado al cliente", "asignar el plan", SUSCRIPCIONES,
            () -> suscripcionService.suscribir(idCliente, idPlan));
    }

    @PostMapping("/suscripcion/cancelar/{id}")
    public String cancelarSuscripcion(@PathVariable Integer id, RedirectAttributes ra) {
        return ejecutar(ra, "Suscripción cancelada", "cancelar la suscripción", SUSCRIPCIONES,
            () -> suscripcionService.cancelar(id));
    }

    @PostMapping("/suscripcion/avisar")
    public String avisarVencimientos(RedirectAttributes ra) {
        try {
            int avisados = suscripcionService.enviarAvisosVencimiento();
            ra.addFlashAttribute("success", avisados == 0
                ? "No había clientes pendientes de aviso"
                : "Se avisó a " + avisados + " cliente(s) de que su plan está por vencer");
        } catch (Exception e) {
            log.error("Error al enviar avisos de vencimiento", e);
            ra.addFlashAttribute("error", "No se pudieron enviar los avisos. Intenta de nuevo.");
        }
        return SUSCRIPCIONES;
    }

    // ================= AUXILIARES =================

    /** Datos comunes a todas las páginas del panel: administrador conectado y sección activa del menú. */
    private void base(Principal principal, Model model, String seccion) {
        administradorService.obtenerPorId(Integer.parseInt(principal.getName()))
            .ifPresent(admin -> model.addAttribute("admin", admin));
        model.addAttribute("seccion", seccion);
    }

    private void aplicarHorario(Clase clase, DayOfWeek dia, LocalTime hora, Integer duracion) {
        if ((dia == null) != (hora == null)) {
            throw new NegocioException("Para programar la clase indica el día y la hora");
        }
        if (duracion != null && (duracion < 10 || duracion > 300)) {
            throw new NegocioException("La duración debe estar entre 10 y 300 minutos");
        }
        clase.setDiaSemana(dia);
        clase.setHora(hora);
        clase.setDuracionMinutos(duracion);
    }

    /**
     * Ejecuta una acción del panel y deja el resultado como mensaje flash. Los errores de negocio
     * se muestran tal cual; los inesperados se registran en el log y el usuario ve un mensaje genérico,
     * para no exponer detalles internos (SQL, nombres de tablas...).
     */
    private String ejecutar(RedirectAttributes ra, String exito, String accion, String destino, Runnable operacion) {
        try {
            operacion.run();
            ra.addFlashAttribute("success", exito);
        } catch (NegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error al {}", accion, e);
            ra.addFlashAttribute("error", "No se pudo " + accion + ". Intenta de nuevo.");
        }
        return destino;
    }

    private boolean tieneErrores(BindingResult result, RedirectAttributes ra) {
        if (!result.hasErrors()) return false;
        ra.addFlashAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
        return true;
    }

    private Cliente buscarCliente(Integer id) {
        return clienteService.obtenerPorId(id).orElseThrow(() -> new NegocioException("El cliente no existe"));
    }

    private Entrenador buscarEntrenador(Integer id) {
        return entrenadorService.obtenerPorId(id).orElseThrow(() -> new NegocioException("El entrenador no existe"));
    }

    private void validarPositivo(Integer valor, String campo) {
        if (valor == null || valor <= 0) {
            throw new NegocioException(campo + " debe ser mayor que 0");
        }
    }
}
