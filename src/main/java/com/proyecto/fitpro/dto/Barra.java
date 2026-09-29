package com.proyecto.fitpro.dto;

import java.util.List;

/**
 * Una barra de un gráfico de reporte. El porcentaje es relativo al valor más alto de la serie,
 * para dibujarla con CSS sin librerías de gráficos.
 */
public record Barra(String etiqueta, long valor, String texto, int porcentaje) {

    public record Dato(String etiqueta, long valor, String texto) {}

    public static List<Barra> escalar(List<Dato> datos) {
        long max = datos.stream().mapToLong(Dato::valor).max().orElse(0);
        return datos.stream()
            .map(d -> new Barra(d.etiqueta(), d.valor(), d.texto(),
                max == 0 ? 0 : (int) Math.max(d.valor() > 0 ? 2 : 0, Math.round(d.valor() * 100.0 / max))))
            .toList();
    }
}
