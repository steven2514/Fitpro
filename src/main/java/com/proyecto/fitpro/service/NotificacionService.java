package com.proyecto.fitpro.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Envía correos a los usuarios. Si no hay servidor SMTP configurado (spring.mail.host), en el
 * perfil dev el correo completo se escribe en el log: así la recuperación de contraseña se puede
 * probar en local sin cuenta de correo. Fuera de dev sólo se registra el destinatario, porque el
 * cuerpo puede llevar un enlace de recuperación válido.
 */
@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String remitente;
    private final boolean desarrollo;

    public NotificacionService(ObjectProvider<JavaMailSender> mailSender,
            @Value("${fitpro.mail.remitente:FitPro <no-responder@fitpro.com>}") String remitente,
            Environment entorno) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.desarrollo = entorno.matchesProfiles("dev");
    }

    /**
     * Igual que {@link #enviar}, pero en otro hilo: quien lo llama no espera al servidor SMTP.
     * Lo usa la recuperación de contraseña para responder igual de rápido exista o no el correo.
     */
    @Async
    public void enviarEnSegundoPlano(String para, String asunto, String cuerpo) {
        enviar(para, asunto, cuerpo);
    }

    /** Devuelve true si el correo salió por SMTP; false si sólo quedó en el log o falló el envío. */
    public boolean enviar(String para, String asunto, String cuerpo) {
        if (para == null || para.isBlank()) {
            return false;
        }
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            if (desarrollo) {
                log.warn("Correo no configurado (spring.mail.host). Mensaje para {} — {}:\n{}", para, asunto, cuerpo);
            } else {
                log.warn("Correo no configurado (spring.mail.host). No se envió «{}» a {}", asunto, para);
            }
            return false;
        }
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(para);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            sender.send(mensaje);
            return true;
        } catch (MailException e) {
            log.error("No se pudo enviar el correo a {}: {}", para, e.getMessage());
            return false;
        }
    }
}
