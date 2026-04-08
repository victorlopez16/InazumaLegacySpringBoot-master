package net.elpuig.inazumalegacy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailRemitente;

    public void enviarCodigoRecuperacion(String emailDestino, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(emailRemitente);
        mensaje.setTo(emailDestino);
        mensaje.setSubject("⚡ Inazuma Legacy – Código de recuperación");
        mensaje.setText(
                "Hola, jugador de Raimon FC.\n\n" +
                        "Tu código de verificación es:\n\n" +
                        "   " + codigo + "\n\n" +
                        "Este código expira en 15 minutos.\n" +
                        "Si no solicitaste esto, ignora este mensaje.\n\n" +
                        "⚡ Inazuma Legacy – Raimon FC"
        );
        mailSender.send(mensaje);
    }
}