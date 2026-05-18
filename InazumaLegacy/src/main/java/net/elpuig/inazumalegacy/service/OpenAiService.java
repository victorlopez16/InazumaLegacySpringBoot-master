package net.elpuig.inazumalegacy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.AddressUtils;
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
                    "Si no conoces una respuesta con total certeza, admítelo en lugar de especular. " +
                    "Responde siempre en español y sé breve (máximo 2-3 frases).";

    public String obtenerRespuestaIA(String preguntaUsuario) {
        try {
            if (openAiApiKey == null || openAiApiKey.isEmpty() || openAiApiKey.contains("tu_clave_aqui")) {
                return "❌ [CONFIG_ERROR]: La API Key no está configurada.";
            }

            // Solución para Railway: Forzar el uso del resolvedor del sistema
            HttpClient httpClient = HttpClient.create()
                    .resolver(spec -> spec.queryTimeout(java.time.Duration.ofSeconds(5)));

            WebClient client = WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .baseUrl("https://api.openai.com")
                    .defaultHeader("Content-Type", "application/json")
                    .defaultHeader("Authorization", "Bearer " + openAiApiKey)
                    .build();

            Map<String, Object> body = Map.of(
                    "model", "gpt-4o",
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user", "content", preguntaUsuario)
                    ),
                    "temperature", 0.3
            );

            // Usamos Map<String, Object> para evitar el aviso de "Raw use of parameterized class"
            @SuppressWarnings("unchecked")
            Map<String, Object> response = client.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("choices")) {
                List<?> choices = (List<?>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<?, ?> firstChoice = (Map<?, ?>) choices.getFirst(); // Uso de getFirst()
                    Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
                    String contenido = (String) message.get("content");
                    return "🤖 [IA]: " + contenido;
                }
            }

            return "⚠️ Error: Formato de respuesta inesperado.";

        } catch (Exception e) {
            logger.error("Error crítico en la conexión con OpenAI: ", e);
            return "❌ [CONEXIÓN_ERROR]: " + e.getMessage();
        }
    }
}