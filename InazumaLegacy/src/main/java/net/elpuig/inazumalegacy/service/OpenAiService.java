package net.elpuig.inazumalegacy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

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
                return "❌ [CONFIG_ERROR]: La API Key no está configurada correctamente.";
            }

            WebClient client = WebClient.builder()
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

            Map response = client.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("choices")) {
                List choices = (List) response.get("choices");
                Map firstChoice = (Map) choices.get(0);
                Map message = (Map) firstChoice.get("message");
                String contenido = (String) message.get("content");

                return "🤖 [IA]: " + contenido;
            }

            return "⚠️ Error: Formato de respuesta inesperado.";

        } catch (Exception e) {
            System.err.println("--- ERROR CRÍTICO EN OPENAI_SERVICE ---");
            e.printStackTrace();
            return "❌ [CONEXIÓN_ERROR]: " + e.getMessage();
        }
    }
}