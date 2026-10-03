package com.proyecto.fitpro.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bloquea temporalmente una IP tras varios inicios de sesión fallidos seguidos, para que no se
 * puedan probar contraseñas sin límite.
 *
 * <p>El conteo vive en memoria: se pierde al reiniciar y, si hubiera varias instancias de la
 * aplicación, cada una llevaría el suyo. Para un único servidor es suficiente.</p>
 */
@Service
public class IntentosLoginService {

    private record Registro(int fallos, Instant bloqueadoHasta) {}

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();
    private final int maxIntentos;
    private final Duration bloqueo;
    private final Clock reloj;

    @Autowired
    public IntentosLoginService(@Value("${fitpro.login.max-intentos:5}") int maxIntentos,
                                @Value("${fitpro.login.minutos-bloqueo:15}") int minutosBloqueo) {
        this(maxIntentos, Duration.ofMinutes(minutosBloqueo), Clock.systemUTC());
    }

    /** Con reloj inyectable, para probar el desbloqueo sin esperar. */
    IntentosLoginService(int maxIntentos, Duration bloqueo, Clock reloj) {
        this.maxIntentos = maxIntentos;
        this.bloqueo = bloqueo;
        this.reloj = reloj;
    }

    /** true si la IP sigue bloqueada por demasiados intentos fallidos. */
    public boolean estaBloqueada(String ip) {
        Registro r = registros.get(ip);
        if (r == null || r.bloqueadoHasta() == null) return false;
        if (reloj.instant().isBefore(r.bloqueadoHasta())) return true;
        registros.remove(ip, r); // el bloqueo ya pasó: se empieza de cero
        return false;
    }

    /** Suma un fallo; al llegar al máximo, la IP queda bloqueada. Devuelve true si quedó bloqueada. */
    public boolean registrarFallo(String ip) {
        Registro nuevo = registros.compute(ip, (k, r) -> {
            int fallos = (r == null ? 0 : r.fallos()) + 1;
            Instant hasta = fallos >= maxIntentos ? reloj.instant().plus(bloqueo) : null;
            return new Registro(fallos, hasta);
        });
        return nuevo.bloqueadoHasta() != null;
    }

    /** Un inicio de sesión correcto borra los fallos acumulados. */
    public void registrarExito(String ip) {
        registros.remove(ip);
    }

    public long minutosBloqueo() {
        return bloqueo.toMinutes();
    }
}
