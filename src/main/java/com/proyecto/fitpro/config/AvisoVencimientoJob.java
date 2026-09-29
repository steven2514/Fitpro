package com.proyecto.fitpro.config;

import com.proyecto.fitpro.service.SuscripcionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Todos los días a las 8:00 avisa por correo a los clientes cuyo plan está por vencer. */
@Component
public class AvisoVencimientoJob {

    private static final Logger log = LoggerFactory.getLogger(AvisoVencimientoJob.class);

    private final SuscripcionService suscripcionService;

    public AvisoVencimientoJob(SuscripcionService suscripcionService) {
        this.suscripcionService = suscripcionService;
    }

    @Scheduled(cron = "${fitpro.avisos.cron:0 0 8 * * *}")
    public void avisarVencimientos() {
        int avisados = suscripcionService.enviarAvisosVencimiento();
        if (avisados > 0) {
            log.info("Avisos de vencimiento enviados: {}", avisados);
        }
    }
}
