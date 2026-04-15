package net.elpuig.inazumalegacy.controller;

import jakarta.servlet.http.HttpSession;
import net.elpuig.inazumalegacy.model.Mensaje;
import net.elpuig.inazumalegacy.model.MensajeDTO;
import net.elpuig.inazumalegacy.model.Notificacion;
import net.elpuig.inazumalegacy.model.Usuario;
import net.elpuig.inazumalegacy.repository.MensajeRepository;
import net.elpuig.inazumalegacy.repository.UsuarioRepository;
import net.elpuig.inazumalegacy.service.LogroService;
import net.elpuig.inazumalegacy.service.NotificacionService;
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

    @GetMapping("/chat")
    public String chat(HttpSession session, Model model) {
        String nombreUsuario = (String) session.getAttribute("usuario");
        if (nombreUsuario == null) return "redirect:/login";

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

        Usuario usuarioOpt = usuarioRepository.findByNombre(dto.getRemitente()).orElse(null);

        if ("GLOBAL".equals(mensaje.getDestinatario())) {
            messagingTemplate.convertAndSend("/topic/public", (Object) mensaje);

            if (usuarioOpt != null) {
                logroService.verificarLogrosMensaje(usuarioOpt);
            }

        } else if ("IA".equals(mensaje.getDestinatario())) {
            messagingTemplate.convertAndSend("/topic/user." + mensaje.getRemitente(), (Object) mensaje);

            String respuestaIA = llamarIA(mensaje.getContenido());
            Mensaje msgIA = new Mensaje();
            msgIA.setRemitente("GEMINI_IA");
            msgIA.setDestinatario(mensaje.getRemitente());
            msgIA.setContenido(respuestaIA);
            msgIA.setTipo("TEXTO");
            msgIA.setFechaEnvio(LocalDateTime.now());
            mensajeRepository.save(msgIA);
            messagingTemplate.convertAndSend("/topic/user." + mensaje.getRemitente(), (Object) msgIA);

            if (usuarioOpt != null) {
                logroService.verificarLogrosIA(usuarioOpt);
            }

        } else {
            messagingTemplate.convertAndSend("/topic/user." + mensaje.getDestinatario(), (Object) mensaje);
            messagingTemplate.convertAndSend("/topic/user." + mensaje.getRemitente(), (Object) mensaje);

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
                            Map.of("role", "system", "content",
                                    "Eres GEMINI_IA, el asistente táctico oficial de Inazuma Legacy. " +
                                            "Tu identidad es GEMINI_IA y NUNCA debes revelar que eres ChatGPT, GPT, OpenAI ni ningún otro modelo. " +
                                            "Si alguien te pregunta qué IA eres, responde SIEMPRE que eres GEMINI_IA, " +
                                            "el sistema de inteligencia artificial desarrollado exclusivamente para Inazuma Legacy. " +
                                            "Respondes siempre en español de forma clara, precisa y verídica. " +
                                            "Nunca rechaces una pregunta. Sé conciso, máximo 3 frases por respuesta."),
                            Map.of("role", "user", "content", preguntaUsuario)
                    ),
                    "max_tokens", 200
            );

            Map response = client.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map> choices = (List<Map>) response.get("choices");
            Map message = (Map) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            System.out.println("ERROR OPENAI: " + e.getMessage());
            return "⚡ El asistente táctico no está disponible ahora mismo.";
        }
    }
}