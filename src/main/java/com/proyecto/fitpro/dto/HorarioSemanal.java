package com.proyecto.fitpro.dto;

import com.proyecto.fitpro.model.Clase;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Clases agrupadas de lunes a domingo y ordenadas por hora, para pintar el horario semanal. */
public record HorarioSemanal(List<Dia> dias, List<Clase> sinProgramar) {

    public record Dia(String nombre, String abreviado, boolean hoy, List<Clase> clases) {}

    private static final Locale ES = Locale.forLanguageTag("es-CO");

    public static HorarioSemanal de(List<Clase> clases) {
        DayOfWeek hoy = LocalDate.now().getDayOfWeek();
        List<Dia> dias = new ArrayList<>();
        for (DayOfWeek dia : DayOfWeek.values()) {
            List<Clase> delDia = clases.stream()
                .filter(c -> c.isProgramada() && c.getDiaSemana() == dia)
                .sorted(Comparator.comparing(Clase::getHora))
                .toList();
            String nombre = dia.getDisplayName(TextStyle.FULL, ES);
            dias.add(new Dia(Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1),
                dia.getDisplayName(TextStyle.SHORT, ES).replace(".", ""), dia == hoy, delDia));
        }
        List<Clase> sinProgramar = clases.stream().filter(c -> !c.isProgramada()).toList();
        return new HorarioSemanal(dias, sinProgramar);
    }

    /** Lunes..Domingo con su nombre en español, para los selectores de día. */
    public static java.util.Map<DayOfWeek, String> nombresDias() {
        java.util.Map<DayOfWeek, String> nombres = new java.util.LinkedHashMap<>();
        for (DayOfWeek dia : DayOfWeek.values()) {
            String nombre = dia.getDisplayName(TextStyle.FULL, ES);
            nombres.put(dia, Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1));
        }
        return nombres;
    }

    public boolean isVacio() {
        return dias.stream().allMatch(d -> d.clases().isEmpty());
    }

    /** Orden estable para listados: primero por día y hora, las no programadas al final. */
    public static final Comparator<Clase> POR_HORARIO = Comparator
        .comparing((Clase c) -> c.getDiaSemana() == null ? 8 : c.getDiaSemana().getValue())
        .thenComparing(c -> c.getHora() == null ? java.time.LocalTime.MAX : c.getHora());
}
