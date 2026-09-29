package com.proyecto.fitpro.dto;

import com.proyecto.fitpro.model.RegistroPeso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Coordenadas de un gráfico de línea (SVG) con la evolución del peso. Se calcula en el servidor
 * para no depender de librerías de JavaScript.
 */
public record GraficoPeso(String puntos, List<Punto> marcas, String pesoMin, String pesoMax,
        String variacion, boolean bajo) {

    // Proporción panorámica: en pantallas anchas el gráfico no debe ocupar media página de alto.
    // Si cambian, hay que ajustar el viewBox y el eje en cliente-panel.html.
    public static final int ANCHO = 900;
    public static final int ALTO = 200;
    private static final int MARGEN = 16;

    public record Punto(double x, double y, String etiqueta) {}

    /** Devuelve null si no hay al menos dos mediciones, porque con una sola no hay línea que dibujar. */
    public static GraficoPeso desde(List<RegistroPeso> historial) {
        if (historial == null || historial.size() < 2) {
            return null;
        }
        double min = historial.stream().mapToDouble(RegistroPeso::getPeso).min().orElse(0);
        double max = historial.stream().mapToDouble(RegistroPeso::getPeso).max().orElse(0);
        // Con todos los pesos iguales se da un rango mínimo para que la línea quede centrada
        double rango = Math.max(max - min, 1.0);
        double base = min - (rango - (max - min)) / 2;

        StringBuilder puntos = new StringBuilder();
        List<Punto> marcas = new ArrayList<>();
        int n = historial.size();
        for (int i = 0; i < n; i++) {
            RegistroPeso r = historial.get(i);
            double x = MARGEN + (ANCHO - 2.0 * MARGEN) * i / (n - 1);
            double y = MARGEN + (ALTO - 2.0 * MARGEN) * (1 - (r.getPeso() - base) / rango);
            puntos.append(String.format(Locale.ROOT, "%.1f,%.1f ", x, y));
            marcas.add(new Punto(round(x), round(y), formato(r.getPeso()) + " kg · " + r.getFecha()));
        }
        double cambio = historial.get(n - 1).getPeso() - historial.get(0).getPeso();
        String variacion = (cambio > 0 ? "+" : "") + formato(cambio) + " kg";
        return new GraficoPeso(puntos.toString().trim(), marcas, formato(min), formato(max), variacion, cambio <= 0);
    }

    private static String formato(double valor) {
        return String.format(Locale.ROOT, "%.1f", valor);
    }

    private static double round(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
