package net.elpuig.inazumalegacy.controller;

import jakarta.servlet.http.HttpSession;
import net.elpuig.inazumalegacy.model.Mensaje;
import net.elpuig.inazumalegacy.model.MensajeDTO;
import net.elpuig.inazumalegacy.repository.MensajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatController {

    @Autowired
    private MensajeRepository mensajeRepo;

    @GetMapping("/chat")
    public String chat(HttpSession session, Model model) {
        String usuario = (String) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";
        model.addAttribute("usuario", usuario);
        model.addAttribute("mensajes", mensajeRepo.findTop50ByOrderByFechaAsc());
        return "chat";
    }

    @MessageMapping("/chat.enviar")
    @SendTo("/topic/chat")
    public Mensaje enviarMensaje(MensajeDTO dto) {
        Mensaje m = new Mensaje();
        m.setAutor(dto.getAutor());
        m.setContenido(dto.getContenido());
        mensajeRepo.save(m);
        return m;
    }
}
