package net.elpuig.inazumalegacy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiService.class);

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private static final String SYSTEM_PROMPT =
            "Eres GEMINI_IA, un asistente de alta precisión. " +
                    "Tu objetivo es dar respuestas veraces, directas y basadas en hechos. " +
                    "Responde siempre en español y sé breve.";

    public String obtenerRespuestaIA(String preguntaUsuario) {
        try {
            if (openAiApiKey == null || openAiApiKey.isEmpty()) {
                return "❌ [CONFIG_ERROR]: API Key faltante.";
            }

            // Usamos RestTemplate en lugar de WebClient para evitar líos de Netty/IPv6
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.openai.com/v1/chat/completions";

            // Configurar Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            // Configurar Body
            Map<String, Object> body = Map.of(
                    "model", "gpt-4o",
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user", "content", preguntaUsuario)
                    ),
                    "temperature", 0.3
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // Hacer la petición (esto es bloqueante, mucho más estable para red)
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

            if (response != null && response.containsKey("choices")) {
                List<?> choices = (List<?>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
                    Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
                    return "🤖 [IA]: " + (String) message.get("content");
                }
            }
            return "⚠️ Error: Respuesta vacía de OpenAI.";

        } catch (Exception e) {
            logger.error("Error en OpenAI con RestTemplate: ", e);
            return "❌ [CONEXIÓN_ERROR]: " + e.getMessage();
        }
    }
}