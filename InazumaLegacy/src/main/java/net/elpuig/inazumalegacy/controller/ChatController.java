package net.elpuig.inazumalegacy.controller;

import jakarta.servlet.http.HttpSession;
import net.elpuig.inazumalegacy.model.*;
import net.elpuig.inazumalegacy.repository.*;
import net.elpuig.inazumalegacy.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
public class ChatController {

    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private LogroService logroService;
    @Autowired private NotificacionService notificacionService;

    @Value("${openai.api.key}")
    private String openAiApiKey;

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
        Mensaje mensaje = new Mensaje();
        mensaje.setRemitente(dto.getRemitente());
        mensaje.setDestinatario(dto.getDestinatario());
        mensaje.setContenido(dto.getContenido());
        mensaje.setTipo(dto.getTipo() != null ? dto.getTipo() : "TEXTO");
        mensaje.setLeido(false);
        mensajeRepository.save(mensaje);

        Usuario remitenteObj = usuarioRepository.findByNombre(dto.getRemitente()).orElse(null);

        // CASO 1: CHAT GLOBAL
        if ("GLOBAL".equals(mensaje.getDestinatario())) {
            messagingTemplate.convertAndSend("/topic/public", mensaje);
            if (remitenteObj != null) logroService.verificarLogrosMensaje(remitenteObj);
        }

        // CASO 2: CONSULTA A LA IA
        else if ("IA".equals(mensaje.getDestinatario())) {
            // Enviamos el eco al usuario
            messagingTemplate.convertAndSendToUser(mensaje.getRemitente(), "/queue/privado", mensaje);

            String respuestaIA = llamarIA(mensaje.getContenido());
            Mensaje msgIA = new Mensaje();
            msgIA.setRemitente("GEMINI_IA");
            msgIA.setDestinatario(mensaje.getRemitente());
            msgIA.setContenido(respuestaIA);
            msgIA.setTipo("TEXTO");
            mensajeRepository.save(msgIA);

            // Enviamos la respuesta solo al usuario que preguntó
            messagingTemplate.convertAndSendToUser(mensaje.getRemitente(), "/queue/privado", msgIA);
            if (remitenteObj != null) logroService.verificarLogrosIA(remitenteObj);
        }

        else {
            messagingTemplate.convertAndSendToUser(mensaje.getDestinatario(), "/queue/privado", mensaje);
            messagingTemplate.convertAndSendToUser(mensaje.getRemitente(), "/queue/privado", mensaje);

            usuarioRepository.findByNombre(mensaje.getDestinatario()).ifPresent(dest ->
                    notificacionService.crear(dest, Notificacion.Tipo.MENSAJE_PRIVADO,
                            "💬 " + mensaje.getRemitente() + " te envió un mensaje privado")
            );
        }
    }

    private String llamarIA(String preguntaUsuario) {
        try {
            WebClient client = WebClient.builder()
                    .baseUrl("https://api.openai.com")
                    .defaultHeader("Content-Type", "application/json")
                    .defaultHeader("Authorization", "Bearer " + openAiApiKey)
                    .build();

            Map<String, Object> body = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(
                            Map.of("role", "system", "content", "Eres GEMINI_IA, el asistente táctico de Inazuma Legacy. Responde en español, máximo 2 frases."),
                            Map.of("role", "user", "content", preguntaUsuario)
                    )
            );

            Map response = client.post().uri("/v1/chat/completions").bodyValue(body).retrieve().bodyToMono(Map.class).block();
            return (String) ((Map) ((List<Map>) response.get("choices")).get(0).get("message")).get("content");
        } catch (Exception e) {
            return "⚡ Error de conexión con el núcleo de la IA.";
        }
    }
}