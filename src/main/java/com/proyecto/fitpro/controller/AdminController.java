package com.proyecto.fitpro.controller;

import com.proyecto.fitpro.model.*;
import com.proyecto.fitpro.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private RutinaService rutinaService;

    @Autowired
    private AlimentacionService alimentacionService;

    @Autowired
    private EntrenadorService entrenadorService;

    @Autowired
    private ClaseService claseService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/panel")
    public String panel(Model model) {
        model.addAttribute("clientes", clienteService.obtenerTodos());
        model.addAttribute("rutinas", rutinaService.obtenerTodas());
        model.addAttribute("alimentaciones", alimentacionService.obtenerTodas());
        model.addAttribute("entrenadores", entrenadorService.obtenerTodos());
        model.addAttribute("clases", claseService.obtenerTodas());
        return "admin-panel";
    }

    // ========== CLIENTES ==========
    @PostMapping("/cliente/crear")
    public String crearCliente(@ModelAttribute Cliente cliente, RedirectAttributes redirectAttributes) {
        try {
            if (cliente.getPassword() != null && !cliente.getPassword().isBlank()) {
                cliente.setPassword(passwordEncoder.encode(cliente.getPassword()));
            }
            clienteService.crear(cliente);
            redirectAttributes.addFlashAttribute("success", "Cliente creado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear cliente: " + e.getMessage());
        }
        return "redirect:/admin/panel";
    }

    @GetMapping("/cliente/editar/{id}")
    public String editarClienteForm(@PathVariable Integer id, Model model) {
        Optional<Cliente> cliente = clienteService.obtenerPorId(id);
        if (cliente.isPresent()) {
            model.addAttribute("cliente", cliente.get());
            return "admin-cliente-editar";
        }
        return "redirect:/admin/panel";
    }

    @PostMapping("/cliente/editar/{id}")
    public String editarCliente(@PathVariable Integer id, @ModelAttribute Cliente cliente, RedirectAttributes redirectAttributes) {
        try {
            cliente.setIdCliente(id);
            if (cliente.getPassword() != null && !cliente.getPassword().isBlank()) {
                cliente.setPassword(passwordEncoder.encode(cliente.getPassword()));
            }
            clienteService.actualizar(cliente);
            redirectAttributes.addFlashAttribute("success", "Cliente actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar cliente: " + e.getMessage());
        }
        return "redirect:/admin/panel";
    }

    @PostMapping("/cliente/eliminar/{id}")
    public String eliminarCliente(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            clienteService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Cliente eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar cliente: " + e.getMessage());
        }
        return "redirect:/admin/panel";
    }

    // ========== RUTINAS ==========
    @PostMapping("/rutina/crear")
    public String crearRutina(@RequestParam Integer idCliente, @RequestParam String nombre,
            @RequestParam String objetivo, @RequestParam String nivel, RedirectAttributes redirectAttributes) {
        try {
            Optional<Cliente> clienteOpt = clienteService.obtenerPorId(idCliente);
            if (clienteOpt.isPresent()) {
                Rutina rutina = new Rutina();
                rutina.setCliente(clienteOpt.get());
                rutina.setNombre(nombre);
                rutina.setObjetivo(objetivo);
                rutina.setNivel(nivel);
                rutinaService.crear(rutina);
                redirectAttributes.addFlashAttribute("success", "Rutina creada exitosamente");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear rutina: " + e.getMessage());
        }
        return "redirect:/admin/panel";
    }

    @GetMapping("/rutina/editar/{id}")
    public String editarRutinaForm(@PathVariable Integer id, Model model) {
        Optional<Rutina> rutina = rutinaService.obtenerPorId(id);
        if (rutina.isPresent()) {
            model.addAttribute("rutina", rutina.get());
            model.addAttribute("clientes", clienteService.obtenerTodos());
            return "admin-rutina-editar";
        }
        return "redirect:/admin/panel";
    }

    @PostMapping("/rutina/editar/{id}")
    public String editarRutina(@PathVariable Integer id, @RequestParam Integer idCliente,
            @RequestParam String nombre, @RequestParam String objetivo, @RequestParam String nivel,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<Rutina> rutinaOpt = rutinaService.obtenerPorId(id);
            Optional<Cliente> clienteOpt = clienteService.obtenerPorId(idCliente);
            if (rutinaOpt.isPresent() && clienteOpt.isPresent()) {
                Rutina rutina = rutinaOpt.get();
                rutina.setCliente(clienteOpt.get());
                rutina.setNombre(nombre);
                rutina.setObjetivo(objetivo);
                rutina.setNivel(nivel);
                rutinaService.actualizar(rutina);
                redirectAttributes.addFlashAttribute("success", "Rutina actualizada exitosamente");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar rutina: " + e.getMessage());
        }
        return "redirect:/admin/panel";
    }

    @PostMapping("/rutina/eliminar/{id}")
    public String eliminarRutina(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            rutinaService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Rutina eliminada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar rutina: " + e.getMessage());
        }
        return "redirect:/admin/panel";
    }

    // ========== ALIMENTACION ==========
    @PostMapping("/alimentacion/crear")
    public String crearAlimentacion(@RequestParam Integer idCliente, @RequestParam String tipoComida,
            @RequestParam Integer calorias, @RequestParam String descripcion,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<Cliente> clienteOpt = clienteService.obtenerPorId(idCliente);
            if (clienteOpt.isPresent()) {
                Alimentacion a = new Alimentacion();
                a.setCliente(clienteOpt.get());
                a.setTipoComida(tipoComida);
                a.setCalorias(calorias);
                a.setDescripcion(descripcion);
                alimentacionService.crear(a);
                redirectAttributes.addFlashAttribute("success", "Plan alimenticio creado exitosamente");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear plan: " + e.getMessage());
        }
        return "redirect:/admin/panel";
    }

    @GetMapping("/alimentacion/editar/{id}")
    public String editarAlimentacionForm(@PathVariable Integer id, Model model) {
        Optional<Alimentacion> a = alimentacionService.obtenerPorId(id);
        if (a.isPresent()) {
            model.addAttribute("alimentacion", a.get());
            model.addAttribute("clientes", clienteService.obtenerTodos());
            return "admin-alimentacion-editar";
        }
        return "redirect:/admin/panel";
    }

    @PostMapping("/alimentacion/editar/{id}")
    public String editarAlimentacion(@PathVariable Integer id, @RequestParam Integer idCliente,
            @RequestParam String tipoComida, @RequestParam Integer calorias,
            @RequestParam String descripcion, RedirectAttributes redirectAttributes) {
        try {
            Optional<Alimentacion> aOpt = alimentacionService.obtenerPorId(id);
            Optional<Cliente> cOpt = clienteService.obtenerPorId(idCliente);
            if (aOpt.isPresent() && cOpt.isPresent()) {
                Alimentacion a = aOpt.get();
                a.setCliente(cOpt.get());
                a.setTipoComida(tipoComida);
                a.setCalorias(calorias);
                a.setDescripcion(descripcion);
                alimentacionService.actualizar(a);
                redirectAttributes.addFlashAttribute("success", "Plan actualizado exitosamente");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar plan: " + e.getMessage());
        }
        return "redirect:/admin/panel";
    }

    @PostMapping("/alimentacion/eliminar/{id}")
    public String eliminarAlimentacion(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            alimentacionService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Plan eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar plan: " + e.getMessage());
        }
        return "redirect:/admin/panel";
    }

    // ========== ENTRENADORES ==========
    @PostMapping("/entrenador/crear")
    public String crearEntrenador(@ModelAttribute Entrenador entrenador, RedirectAttributes redirectAttributes) {
        try {
            entrenadorService.crear(entrenador);
            redirectAttributes.addFlashAttribute("success", "Entrenador creado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear entrenador: " + e.getMessage());
        }
        return "redirect:/admin/panel";
    }

    @GetMapping("/entrenador/editar/{id}")
    public String editarEntrenadorForm(@PathVariable Integer id, Model model) {
        Optional<Entrenador> e = entrenadorService.obtenerPorId(id);
        if (e.isPresent()) {
            model.addAttribute("entrenador", e.get());
            return "admin-entrenador-editar";
        }
        return "redirect:/admin/panel";
    }

    @PostMapping("/entrenador/editar/{id}")
    public String editarEntrenador(@PathVariable Integer id, @ModelAttribute Entrenador entrenador,
            RedirectAttributes redirectAttributes) {
        try {
            entrenador.setIdEntrenador(id);
            entrenadorService.actualizar(entrenador);
            redirectAttributes.addFlashAttribute("success", "Entrenador actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar entrenador: " + e.getMessage());
        }
        return "redirect:/admin/panel";
    }

    @PostMapping("/entrenador/eliminar/{id}")
    public String eliminarEntrenador(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            entrenadorService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Entrenador eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar entrenador: " + e.getMessage());
        }
        return "redirect:/admin/panel";
    }

    // ========== CLASES ==========
    @PostMapping("/clase/crear")
    public String crearClase(@RequestParam String nombre, @RequestParam String descripcion,
            @RequestParam Integer capacidad, @RequestParam Integer idEntrenador,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<Entrenador> eOpt = entrenadorService.obtenerPorId(idEntrenador);
            if (eOpt.isPresent()) {
                Clase clase = new Clase();
                clase.setNombre(nombre);
                clase.setDescripcion(descripcion);
                clase.setCapacidad(capacidad);
                clase.setEntrenador(eOpt.get());
                claseService.crear(clase);
                redirectAttributes.addFlashAttribute("success", "Clase creada exitosamente");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear clase: " + e.getMessage());
        }
        return "redirect:/admin/panel";
    }

    @GetMapping("/clase/editar/{id}")
    public String editarClaseForm(@PathVariable Integer id, Model model) {
        Optional<Clase> c = claseService.obtenerPorId(id);
        if (c.isPresent()) {
            model.addAttribute("clase", c.get());
            model.addAttribute("entrenadores", entrenadorService.obtenerTodos());
            return "admin-clase-editar";
        }
        return "redirect:/admin/panel";
    }

    @PostMapping("/clase/editar/{id}")
    public String editarClase(@PathVariable Integer id, @RequestParam String nombre,
            @RequestParam String descripcion, @RequestParam Integer capacidad,
            @RequestParam Integer idEntrenador, RedirectAttributes redirectAttributes) {
        try {
            Optional<Clase> cOpt = claseService.obtenerPorId(id);
            Optional<Entrenador> eOpt = entrenadorService.obtenerPorId(idEntrenador);
            if (cOpt.isPresent() && eOpt.isPresent()) {
                Clase c = cOpt.get();
                c.setNombre(nombre);
                c.setDescripcion(descripcion);
                c.setCapacidad(capacidad);
                c.setEntrenador(eOpt.get());
                claseService.actualizar(c);
                redirectAttributes.addFlashAttribute("success", "Clase actualizada exitosamente");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar clase: " + e.getMessage());
        }
        return "redirect:/admin/panel";
    }

    @PostMapping("/clase/eliminar/{id}")
    public String eliminarClase(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            claseService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Clase eliminada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar clase: " + e.getMessage());
        }
        return "redirect:/admin/panel";
    }

    @PostMapping("/clase/inscribir")
    public String inscribirCliente(@RequestParam Integer idCliente, @RequestParam Integer idClase,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<Cliente> cOpt = clienteService.obtenerPorId(idCliente);
            Optional<Clase> clOpt = claseService.obtenerPorId(idClase);
            if (cOpt.isPresent() && clOpt.isPresent()) {
                Cliente cliente = cOpt.get();
                if (cliente.getClases() == null) cliente.setClases(new ArrayList<>());
                boolean yaInscrito = cliente.getClases().stream()
                    .anyMatch(c -> c.getIdClase().equals(idClase));
                if (yaInscrito) {
                    redirectAttributes.addFlashAttribute("error", "El cliente ya está inscrito");
                } else {
                    cliente.getClases().add(clOpt.get());
                    clienteService.actualizar(cliente);
                    redirectAttributes.addFlashAttribute("success", "Cliente inscrito exitosamente");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al inscribir: " + e.getMessage());
        }
        return "redirect:/admin/panel";
    }

    @GetMapping("/cliente/buscar")
    public String buscarCliente(@RequestParam(required = false) String documento, Model model) {
        model.addAttribute("clientes", clienteService.obtenerTodos());
        model.addAttribute("rutinas", rutinaService.obtenerTodas());
        model.addAttribute("alimentaciones", alimentacionService.obtenerTodas());
        model.addAttribute("entrenadores", entrenadorService.obtenerTodos());
        model.addAttribute("clases", claseService.obtenerTodas());
        if (documento != null && !documento.trim().isEmpty()) {
            Optional<Cliente> c = clienteService.obtenerPorDocumento(documento);
            c.ifPresent(cliente -> model.addAttribute("clienteEncontrado", cliente));
            model.addAttribute("documentoBuscado", documento);
        }
        return "admin-panel";
    }
}
