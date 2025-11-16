package es.upm.cervezas.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public void send(String to, String subject, String body) {
        log.info("[Correo simulado] Para: {} | Asunto: {} | Contenido: {}", to, subject, body);
    }
}
