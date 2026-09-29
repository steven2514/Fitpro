package com.proyecto.fitpro.service.impl;

import com.proyecto.fitpro.exception.NegocioException;
import com.proyecto.fitpro.model.Cliente;
import com.proyecto.fitpro.model.Plan;
import com.proyecto.fitpro.model.Suscripcion;
import com.proyecto.fitpro.repository.ClienteRepository;
import com.proyecto.fitpro.repository.PlanRepository;
import com.proyecto.fitpro.repository.SuscripcionRepository;
import com.proyecto.fitpro.service.NotificacionService;
import com.proyecto.fitpro.service.SuscripcionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SuscripcionServiceImpl implements SuscripcionService {

    private final SuscripcionRepository suscripcionRepository;
    private final ClienteRepository clienteRepository;
    private final PlanRepository planRepository;
    private final NotificacionService notificacionService;

    public SuscripcionServiceImpl(SuscripcionRepository suscripcionRepository, ClienteRepository clienteRepository,
            PlanRepository planRepository, NotificacionService notificacionService) {
        this.suscripcionRepository = suscripcionRepository;
        this.clienteRepository = clienteRepository;
        this.planRepository = planRepository;
        this.notificacionService = notificacionService;
    }

    @Override
    public Suscripcion suscribir(Integer idCliente, Integer idPlan) {
        Cliente cliente = clienteRepository.findById(idCliente)
            .orElseThrow(() -> new NegocioException("El cliente no existe"));
        Plan plan = planRepository.findById(idPlan)
            .orElseThrow(() -> new NegocioException("El plan no existe"));
        if (!plan.isActivo()) {
            throw new NegocioException("El plan \"" + plan.getNombre() + "\" ya no está disponible");
        }
        obtenerVigente(idCliente).ifPresent(actual -> {
            if (actual.getPlan().getIdPlan().equals(idPlan)) {
                throw new NegocioException("Ya tienes activo el plan " + plan.getNombre());
            }
        });
        // Un cliente sólo tiene una suscripción activa: el cambio de plan cierra la anterior
        cerrarActivas(idCliente);

        LocalDate hoy = LocalDate.now();
        Suscripcion nueva = new Suscripcion();
        nueva.setCliente(cliente);
        nueva.setPlan(plan);
        nueva.setFechaInicio(hoy);
        nueva.setFechaFin(hoy.plusDays(plan.getDuracionDias()));
        nueva.setPrecioPagado(plan.getPrecioMensual());
        nueva.setEstado(Suscripcion.Estado.ACTIVA);
        return suscripcionRepository.save(nueva);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Suscripcion> obtenerVigente(Integer idCliente) {
        return suscripcionRepository.findByCliente_IdClienteAndEstado(idCliente, Suscripcion.Estado.ACTIVA).stream()
            .filter(Suscripcion::isVigente)
            .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Suscripcion> obtenerHistorial(Integer idCliente) {
        return suscripcionRepository.findByCliente_IdClienteOrderByFechaInicioDesc(idCliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Suscripcion> obtenerVigentes() {
        return suscripcionRepository.findVigentes(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public long contarVigentesPorPlan(Integer idPlan) {
        return suscripcionRepository.countVigentesPorPlan(idPlan, LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public long ingresosMensuales() {
        return obtenerVigentes().stream().mapToLong(Suscripcion::getPrecioPagado).sum();
    }

    @Override
    public void cancelarVigente(Integer idCliente) {
        if (obtenerVigente(idCliente).isEmpty()) {
            throw new NegocioException("No tienes una suscripción activa");
        }
        cerrarActivas(idCliente);
    }

    @Override
    public void cancelar(Integer idSuscripcion) {
        Suscripcion s = suscripcionRepository.findById(idSuscripcion)
            .orElseThrow(() -> new NegocioException("La suscripción no existe"));
        s.setEstado(Suscripcion.Estado.CANCELADA);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Suscripcion> obtenerPorVencer() {
        return obtenerVigentes().stream().filter(Suscripcion::isPorVencer).toList();
    }

    @Override
    public int enviarAvisosVencimiento() {
        int avisados = 0;
        for (Suscripcion s : obtenerPorVencer()) {
            if (s.yaFueAvisada()) continue;
            Cliente c = s.getCliente();
            String cuando = s.getDiasRestantes() == 0 ? "hoy" : "en " + s.getDiasRestantes() + " día(s)";
            notificacionService.enviar(c.getEmail(), "FitPro - Tu plan " + s.getPlan().getNombre() + " vence " + cuando, """
                Hola %s,

                Tu plan %s vence %s (el %s).
                Renuévalo desde tu panel para no perder el acceso a tus clases y a tu seguimiento.

                ¡Nos vemos en el gimnasio!
                """.formatted(c.getNombre(), s.getPlan().getNombre(), cuando,
                    s.getFechaFin().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            // Se marca aunque el cliente no tenga email: el aviso también se ve en su panel
            s.setAvisoVencimientoEnviado(true);
            avisados++;
        }
        return avisados;
    }

    private void cerrarActivas(Integer idCliente) {
        suscripcionRepository.findByCliente_IdClienteAndEstado(idCliente, Suscripcion.Estado.ACTIVA)
            .forEach(s -> s.setEstado(Suscripcion.Estado.CANCELADA));
    }
}
