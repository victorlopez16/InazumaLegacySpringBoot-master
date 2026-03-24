package net.elpuig.inazumalegacy.controller;

import net.elpuig.inazumalegacy.model.Mensaje;
import net.elpuig.inazumalegacy.repository.MensajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MensajeRepository mensajeRepository;

    /**
     * Ruta para acceder a la página del chat
     */
    @GetMapping("/chat")
    public String irAlChat(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("nombreUsuario", principal.getName());
        } else {
            // Si no hay sesión, podrías redirigir al login o usar un nombre temporal
            model.addAttribute("nombreUsuario", "Jugador_Raimon");
        }
        return "chat"; // Busca chat.html en src/main/resources/templates
    }

    /**
     * Gestión de mensajes en tiempo real vía WebSocket
     */
    @MessageMapping("/chat.send")
    public void processMessage(@Payload Mensaje mensaje) {
        // Aseguramos que la fecha se asigne antes de guardar si el prePersist falla
        if (mensaje.getFechaEnvio() == null) {
            mensaje.setFechaEnvio(LocalDateTime.now());
        }

        // 1. Guardar el mensaje original en la base de datos
        mensajeRepository.save(mensaje);

        // 2. Lógica de distribución de mensajes
        if ("GLOBAL".equals(mensaje.getDestinatario())) {
            // Enviar al canal público (todos los suscritos a /topic/public lo verán)
            messagingTemplate.convertAndSend("/topic/public", mensaje);

        } else if ("IA".equals(mensaje.getDestinatario())) {
            // El mensaje del usuario solo lo ve él en su pantalla
            messagingTemplate.convertAndSendToUser(mensaje.getRemitente(), "/queue/messages", mensaje);

            // Generamos mi respuesta (Gemini IA)
            Mensaje respuestaIA = new Mensaje();
            respuestaIA.setRemitente("GEMINI_IA");
            respuestaIA.setDestinatario(mensaje.getRemitente());
            respuestaIA.setTipo("TEXTO");
            respuestaIA.setFechaEnvio(LocalDateTime.now());
            respuestaIA.setContenido("⚡ [SISTEMA INAZUMA] Analizando datos de campo... ¡Hola, " + mensaje.getRemitente() + "! Soy tu asistente táctico. ¿Quieres que revisemos las estadísticas de algún jugador o prefieres cambiar la formación del equipo?");

            // Guardamos mi respuesta también para que quede constancia en el historial
            mensajeRepository.save(respuestaIA);

            // Enviamos la respuesta solo al usuario que preguntó
            messagingTemplate.convertAndSendToUser(mensaje.getRemitente(), "/queue/messages", respuestaIA);

        } else {
            // Chat Privado entre dos personas (Usuario A -> Usuario B)
            // Se lo enviamos al destinatario
            messagingTemplate.convertAndSendToUser(mensaje.getDestinatario(), "/queue/messages", mensaje);

            // También se lo enviamos al remitente para que aparezca en su propia ventana de chat
            messagingTemplate.convertAndSendToUser(mensaje.getRemitente(), "/queue/messages", mensaje);
        }
    }
}