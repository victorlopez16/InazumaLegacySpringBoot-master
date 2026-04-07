package net.elpuig.inazumalegacy.controller;

import net.elpuig.inazumalegacy.model.Mensaje;
import net.elpuig.inazumalegacy.repository.MensajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MensajeRepository mensajeRepository;

    @Value("${anthropic.api.key}")
    private String anthropicApiKey;

    @GetMapping("/chat")
    public String irAlChat(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("nombreUsuario", principal.getName());
        } else {
            model.addAttribute("nombreUsuario", "Jugador_Raimon");
        }
        return "chat";
    }

    @MessageMapping("/chat.send")
    public void processMessage(@Payload Mensaje mensaje) {
        if (mensaje.getFechaEnvio() == null) {
            mensaje.setFechaEnvio(LocalDateTime.now());
        }

        mensajeRepository.save(mensaje);

        if ("GLOBAL".equals(mensaje.getDestinatario())) {
            messagingTemplate.convertAndSend("/topic/public", mensaje);

        } else if ("IA".equals(mensaje.getDestinatario())) {
            // Mostrar el mensaje del usuario en su pantalla
            messagingTemplate.convertAndSendToUser(
                    mensaje.getRemitente(), "/queue/messages", mensaje);

            // Llamar a la API de Claude
            String respuestaTexto = llamarClaudeAPI(mensaje.getContenido());

            Mensaje respuestaIA = new Mensaje();
            respuestaIA.setRemitente("CLAUDE_IA");
            respuestaIA.setDestinatario(mensaje.getRemitente());
            respuestaIA.setTipo("TEXTO");
            respuestaIA.setFechaEnvio(LocalDateTime.now());
            respuestaIA.setContenido(respuestaTexto);

            mensajeRepository.save(respuestaIA);
            messagingTemplate.convertAndSendToUser(
                    mensaje.getRemitente(), "/queue/messages", respuestaIA);

        } else {
            messagingTemplate.convertAndSendToUser(
                    mensaje.getDestinatario(), "/queue/messages", mensaje);
            messagingTemplate.convertAndSendToUser(
                    mensaje.getRemitente(), "/queue/messages", mensaje);
        }
    }

    private String llamarClaudeAPI(String preguntaUsuario) {
        try {
            WebClient client = WebClient.builder()
                    .baseUrl("https://api.anthropic.com")
                    .defaultHeader("x-api-key", anthropicApiKey)
                    .defaultHeader("anthropic-version", "2023-06-01")
                    .defaultHeader("content-type", "application/json")
                    .build();

            Map<String, Object> body = Map.of(
                    "model", "claude-sonnet-4-20250514",
                    "max_tokens", 500,
                    "system", """
                    Eres el asistente táctico oficial de Inazuma Legacy, un juego de fútbol.
                    Tu nombre es CLAUDE_IA. Respondes siempre en español, de forma concisa
                    y con el estilo épico del universo Inazuma Eleven. Puedes hablar de
                    jugadores, tácticas, temporadas y estadísticas del equipo Raimon FC.
                    Máximo 3 frases por respuesta.
                    """,
                    "messages", List.of(
                            Map.of("role", "user", "content", preguntaUsuario)
                    )
            );

            Map response = client.post()
                    .uri("/v1/messages")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map> content = (List<Map>) response.get("content");
            return (String) content.get(0).get("text");

        } catch (Exception e) {
            return "⚡ [ERROR] No se pudo contactar con el asistente táctico. Inténtalo de nuevo.";
        }
    }
}