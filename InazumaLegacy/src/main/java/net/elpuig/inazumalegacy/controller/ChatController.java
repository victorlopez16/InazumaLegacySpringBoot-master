package net.elpuig.inazumalegacy.controller;

import jakarta.servlet.http.HttpSession;
import net.elpuig.inazumalegacy.model.Mensaje;
import net.elpuig.inazumalegacy.model.MensajeDTO;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MensajeRepository mensajeRepository;

    @Value("${groq.api.key}")
    private String groqApiKey;

    @GetMapping("/chat")
    public String chat(HttpSession session, Model model) {
        String nombreUsuario = (String) session.getAttribute("usuario");
        if (nombreUsuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("nombreUsuario", nombreUsuario);

        List<Mensaje> historial = mensajeRepository.findTop50ByDestinatarioOrderByFechaEnvioAsc("GLOBAL");
        model.addAttribute("historial", historial);

        return "chat";
    }

    @MessageMapping("/chat.send")
    public void procesarMensaje(@Payload MensajeDTO dto) {
        Mensaje mensaje = new Mensaje();
        mensaje.setRemitente(dto.getRemitente());
        mensaje.setDestinatario(dto.getDestinatario());
        mensaje.setContenido(dto.getContenido());
        mensaje.setTipo(dto.getTipo() != null ? dto.getTipo() : "TEXTO");
        mensaje.setFechaEnvio(LocalDateTime.now());
        mensajeRepository.save(mensaje);

        if ("GLOBAL".equals(mensaje.getDestinatario())) {
            messagingTemplate.convertAndSend("/topic/public", mensaje);

        } else if ("IA".equals(mensaje.getDestinatario())) {
            // Eco del mensaje propio al usuario
            messagingTemplate.convertAndSend(
                    "/topic/user." + mensaje.getRemitente(), mensaje);

            String respuestaIA = llamarIA(mensaje.getContenido());
            Mensaje msgIA = new Mensaje();
            msgIA.setRemitente("GEMINI_IA");
            msgIA.setDestinatario(mensaje.getRemitente());
            msgIA.setContenido(respuestaIA);
            msgIA.setTipo("TEXTO");
            msgIA.setFechaEnvio(LocalDateTime.now());
            mensajeRepository.save(msgIA);
            messagingTemplate.convertAndSend(
                    "/topic/user." + mensaje.getRemitente(), msgIA);

        } else {
            // Mensaje privado entre usuarios
            messagingTemplate.convertAndSend(
                    "/topic/user." + mensaje.getDestinatario(), mensaje);
            messagingTemplate.convertAndSend(
                    "/topic/user." + mensaje.getRemitente(), mensaje);
        }
    }

    private String llamarIA(String preguntaUsuario) {
        try {
            WebClient client = WebClient.builder()
                    .baseUrl("https://api.groq.com")
                    .defaultHeader("Content-Type", "application/json")
                    .defaultHeader("Authorization", "Bearer " + groqApiKey)
                    .build();

            Map<String, Object> body = Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "messages", List.of(
                            Map.of("role", "system", "content",
                                    "Eres un asistente inteligente llamado GEMINI_IA. " +
                                            "Respondes siempre en español de forma clara, precisa y verídica. " +
                                            "Respondes cualquier pregunta sin importar el tema: ciencia, historia, " +
                                            "matemáticas, tecnología, cultura general, etc. " +
                                            "Nunca rechaces una pregunta. Sé conciso, máximo 3 frases por respuesta."),
                            Map.of("role", "user", "content", preguntaUsuario)
                    ),
                    "max_tokens", 200
            );

            Map response = client.post()
                    .uri("/openai/v1/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map> choices = (List<Map>) response.get("choices");
            Map message = (Map) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            System.out.println("ERROR GROQ: " + e.getMessage());
            return "⚡ El asistente táctico no está disponible ahora mismo.";
        }
    }
}