package com.proyecto.fitpro.config;

import com.proyecto.fitpro.model.Plan;
import com.proyecto.fitpro.repository.PlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** Crea los tres planes de suscripción por defecto la primera vez que arranca la aplicación. */
@Configuration
public class PlanesInicialesConfig {

    private static final Logger log = LoggerFactory.getLogger(PlanesInicialesConfig.class);

    @Bean
    public ApplicationRunner crearPlanesIniciales(PlanRepository planRepository) {
        return args -> {
            if (planRepository.count() > 0) {
                return;
            }
            planRepository.saveAll(List.of(
                plan("Básico", "Para empezar a entrenar a tu ritmo.", 69_900, false, false, """
                    Acceso a la zona de pesas y cardio
                    Rutina personalizada
                    Seguimiento de IMC y progreso"""),
                plan("Pro", "El más elegido: entrena y únete a las clases grupales.", 109_900, true, true, """
                    Todo lo del plan Básico
                    Clases grupales ilimitadas
                    Plan de alimentación personalizado"""),
                plan("Élite", "Acompañamiento completo de tu entrenador.", 169_900, true, false, """
                    Todo lo del plan Pro
                    Revisión mensual con tu entrenador
                    Prioridad en cupos de clases""")
            ));
            log.info("Planes de suscripción iniciales creados");
        };
    }

    private static Plan plan(String nombre, String descripcion, int precio, boolean incluyeClases,
            boolean destacado, String beneficios) {
        Plan p = new Plan();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecioMensual(precio);
        p.setDuracionDias(30);
        p.setIncluyeClases(incluyeClases);
        p.setDestacado(destacado);
        p.setActivo(true);
        p.setBeneficios(beneficios);
        return p;
    }
}
