package com.proyecto.fitpro.config;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class IntentosLoginServiceTest {

    /** Reloj que avanza a mano, para comprobar el desbloqueo sin esperar. */
    private static class RelojManual extends Clock {
        private Instant ahora = Instant.parse("2026-01-01T10:00:00Z");
        void avanzar(Duration d) { ahora = ahora.plus(d); }
        @Override public Instant instant() { return ahora; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zona) { return this; }
    }

    private final RelojManual reloj = new RelojManual();
    private final IntentosLoginService servicio = new IntentosLoginService(3, Duration.ofMinutes(15), reloj);

    @Test
    void bloqueaAlLlegarAlMaximoYDesbloqueaAlPasarElTiempo() {
        assertFalse(servicio.registrarFallo("1.1.1.1"));
        assertFalse(servicio.registrarFallo("1.1.1.1"));
        assertTrue(servicio.registrarFallo("1.1.1.1"));
        assertTrue(servicio.estaBloqueada("1.1.1.1"));
        assertFalse(servicio.estaBloqueada("2.2.2.2"), "otra IP no se ve afectada");

        reloj.avanzar(Duration.ofMinutes(14));
        assertTrue(servicio.estaBloqueada("1.1.1.1"));
        reloj.avanzar(Duration.ofMinutes(2));
        assertFalse(servicio.estaBloqueada("1.1.1.1"));
        assertFalse(servicio.registrarFallo("1.1.1.1"), "tras el bloqueo el conteo empieza de cero");
    }

    @Test
    void unLoginCorrectoBorraLosFallos() {
        servicio.registrarFallo("1.1.1.1");
        servicio.registrarFallo("1.1.1.1");
        servicio.registrarExito("1.1.1.1");
        assertFalse(servicio.registrarFallo("1.1.1.1"));
        assertFalse(servicio.registrarFallo("1.1.1.1"));
    }
}
