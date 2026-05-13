package net.elpuig.inazumalegacy.controller;

import jakarta.servlet.http.HttpSession;
import net.elpuig.inazumalegacy.model.*;
import net.elpuig.inazumalegacy.repository.*;
import net.elpuig.inazumalegacy.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatController {

    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private NotificacionService notificacionService;
    @Autowired private OpenAiService openAiService;

    @GetMapping("/social")
    public String social(HttpSession session, Model model) {
        String nombreUsuario = (String) session.getAttribute("usuario");
        if (nombreUsuario == null) return "redirect:/login";

        model.addAttribute("nombreUsuario", nombreUsuario);
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("historialGlobal", mensajeRepository.findTop50ByDestinatarioOrderByFechaEnvioAsc("GLOBAL"));

        long noLeidos = mensajeRepository.countByDestinatarioAndLeidoFalse(nombreUsuario);
        model.addAttribute("mensajesNuevos", noLeidos);

        return "chat";
    }

    @MessageMapping("/chat.send")
    public void procesarMensaje(@Payload MensajeDTO dto) {
        // Persistencia en Base de Datos
        Mensaje mensaje = new Mensaje();
        mensaje.setRemitente(dto.getRemitente());
        mensaje.setDestinatario(dto.getDestinatario());
        mensaje.setContenido(dto.getContenido());
        mensaje.setTipo(dto.getTipo() != null ? dto.getTipo() : "TEXTO");
        mensaje.setLeido(false);
        mensajeRepository.save(mensaje);

        // --- ENRUTAMIENTO DINÁMICO ---

        // 1. CHAT GLOBAL
        if ("GLOBAL".equals(mensaje.getDestinatario())) {
            messagingTemplate.convertAndSend("/topic/public", mensaje);
        }

        // 2. CONSULTA A LA IA (Bypassing de seguridad para Gemini)
        else if ("IA".equals(mensaje.getDestinatario())) {
            // Enviamos tu propio mensaje a tu cola personal para que aparezca en pantalla
            messagingTemplate.convertAndSend("/queue/mensajes-" + mensaje.getRemitente(), mensaje);

            // Llamada al servicio de IA (OpenAiService que gestiona Gemini)
            String respuestaIA = openAiService.obtenerRespuestaIA(mensaje.getContenido());

            Mensaje msgIA = new Mensaje();
            msgIA.setRemitente("GEMINI_IA");
            msgIA.setDestinatario(mensaje.getRemitente());
            msgIA.setContenido(respuestaIA);
            msgIA.setTipo("TEXTO");
            mensajeRepository.save(msgIA);

            // Enviamos la respuesta de la IA a tu cola personal
            messagingTemplate.convertAndSend("/queue/mensajes-" + mensaje.getRemitente(), msgIA);
        }

        // 3. MENSAJES PRIVADOS
        else {
            // Enviamos el mensaje a la cola del destinatario y a la del remitente
            messagingTemplate.convertAndSend("/queue/mensajes-" + mensaje.getDestinatario(), mensaje);
            messagingTemplate.convertAndSend("/queue/mensajes-" + mensaje.getRemitente(), mensaje);

            usuarioRepository.findByNombre(mensaje.getDestinatario()).ifPresent(dest ->
                    notificacionService.crear(dest, Notificacion.Tipo.MENSAJE_PRIVADO,
                            "💬 " + mensaje.getRemitente() + " te envió un mensaje privado")
            );
        }
    }
}