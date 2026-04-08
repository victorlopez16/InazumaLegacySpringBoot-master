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
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private String llamarClaudeAPI(String preguntaUsuario) {
        try {
            WebClient client = WebClient.builder()
                    .baseUrl("https://generativelanguage.googleapis.com")
                    .defaultHeader("Content-Type", "application/json")
                    .build();

            Map<String, Object> part = Map.of("text", """
                    Eres el asistente táctico oficial de Inazuma Legacy.
                    Tu nombre es GEMINI_IA. Respondes siempre en español,
                    de forma concisa y con el estilo épico del universo
                    Inazuma Eleven. Máximo 3 frases por respuesta.
                    Pregunta del usuario: """ + preguntaUsuario);

            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(part))
                    )
            );

            Map response = client.post()
                    .uri("/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map> candidates = (List<Map>) response.get("candidates");
            Map content = (Map) candidates.get(0).get("content");
            List<Map> parts = (List<Map>) content.get("parts");
            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            return "⚡ [ERROR] No se pudo contactar con el asistente táctico.";
        }
    }
}
