package com.proyecto.fitpro.service;

import com.proyecto.fitpro.dto.Barra;
import com.proyecto.fitpro.model.Clase;
import com.proyecto.fitpro.model.Cliente;
import com.proyecto.fitpro.model.Suscripcion;
import com.proyecto.fitpro.repository.ClaseRepository;
import com.proyecto.fitpro.repository.ClienteRepository;
import com.proyecto.fitpro.repository.SuscripcionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/** Cifras para la página de reportes del administrador. */
@Service
@Transactional(readOnly = true)
public class ReporteService {

    private static final Locale ES = Locale.forLanguageTag("es-CO");

    private final SuscripcionRepository suscripcionRepository;
    private final ClienteRepository clienteRepository;
    private final ClaseRepository claseRepository;

    public ReporteService(SuscripcionRepository suscripcionRepository, ClienteRepository clienteRepository,
            ClaseRepository claseRepository) {
        this.suscripcionRepository = suscripcionRepository;
        this.clienteRepository = clienteRepository;
        this.claseRepository = claseRepository;
    }

    /** Ventas de planes por mes de contratación (incluye las que luego se cancelaron: el pago ya se hizo). */
    public List<Barra> ingresosPorMes(int meses) {
        Map<YearMonth, Long> porMes = suscripcionRepository.findAll().stream()
            .collect(Collectors.groupingBy(s -> YearMonth.from(s.getFechaInicio()),
                Collectors.summingLong(Suscripcion::getPrecioPagado)));
        return Barra.escalar(ultimosMeses(meses).stream()
            .map(m -> {
                long total = porMes.getOrDefault(m, 0L);
                return new Barra.Dato(nombreMes(m), total, pesos(total));
            }).toList());
    }

    public List<Barra> clientesNuevosPorMes(int meses) {
        Map<YearMonth, Long> porMes = clienteRepository.findAll().stream()
            .filter(c -> c.getFechaRegistro() != null)
            .collect(Collectors.groupingBy(c -> YearMonth.from(c.getFechaRegistro()), Collectors.counting()));
        return Barra.escalar(ultimosMeses(meses).stream()
            .map(m -> {
                long total = porMes.getOrDefault(m, 0L);
                return new Barra.Dato(nombreMes(m), total, String.valueOf(total));
            }).toList());
    }

    /** Suscripciones vigentes por plan, de más a menos. */
    public List<Barra> suscripcionesPorPlan() {
        Map<String, Long> porPlan = suscripcionRepository.findVigentes(LocalDate.now()).stream()
            .collect(Collectors.groupingBy(s -> s.getPlan().getNombre(), LinkedHashMap::new, Collectors.counting()));
        return Barra.escalar(porPlan.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .map(e -> new Barra.Dato(e.getKey(), e.getValue(), e.getValue() + (e.getValue() == 1 ? " activa" : " activas")))
            .toList());
    }

    /** Clases con capacidad definida, de la más llena a la más vacía. */
    public List<Barra> ocupacionClases(int limite) {
        List<Barra.Dato> datos = claseRepository.findAll().stream()
            .filter(c -> c.getCapacidad() != null && c.getCapacidad() > 0)
            .sorted(Comparator.comparingInt(Clase::getOcupacion).reversed())
            .limit(limite)
            .map(c -> new Barra.Dato(c.getNombre(), c.getOcupacion(),
                c.getInscritos() + "/" + c.getCapacidad() + " (" + c.getOcupacion() + "%)"))
            .toList();
        // Aquí el 100% es la capacidad, no el valor más alto de la serie
        return datos.stream().map(d -> new Barra(d.etiqueta(), d.valor(), d.texto(), (int) d.valor())).toList();
    }

    public long ingresosDelMes() {
        YearMonth actual = YearMonth.now();
        return suscripcionRepository.findAll().stream()
            .filter(s -> YearMonth.from(s.getFechaInicio()).equals(actual))
            .mapToLong(Suscripcion::getPrecioPagado).sum();
    }

    public long clientesNuevosDelMes() {
        YearMonth actual = YearMonth.now();
        List<Cliente> clientes = clienteRepository.findAll();
        return clientes.stream()
            .filter(c -> c.getFechaRegistro() != null && YearMonth.from(c.getFechaRegistro()).equals(actual))
            .count();
    }

    public int ocupacionPromedio() {
        return (int) Math.round(claseRepository.findAll().stream()
            .filter(c -> c.getCapacidad() != null && c.getCapacidad() > 0)
            .mapToInt(Clase::getOcupacion).average().orElse(0));
    }

    public static String pesos(long valor) {
        return String.format(ES, "$%,d", valor);
    }

    private static List<YearMonth> ultimosMeses(int meses) {
        List<YearMonth> lista = new ArrayList<>();
        YearMonth actual = YearMonth.now();
        for (int i = meses - 1; i >= 0; i--) {
            lista.add(actual.minusMonths(i));
        }
        return lista;
    }

    private static String nombreMes(YearMonth mes) {
        String nombre = mes.getMonth().getDisplayName(TextStyle.SHORT, ES).replace(".", "");
        return Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1) + " " + String.valueOf(mes.getYear()).substring(2);
    }
}
