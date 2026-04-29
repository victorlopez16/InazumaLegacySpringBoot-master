package net.elpuig.inazumalegacy.controller;

import net.elpuig.inazumalegacy.model.Usuario;
import net.elpuig.inazumalegacy.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class RankingController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/ranking")
    public String mostrarRanking(Model model) {
        List<Usuario> topJugadores = usuarioRepository.findTop10ByOrderByPuntosInazumaDesc();

        model.addAttribute("jugadores", topJugadores);
        return "ranking"; // Buscará el archivo ranking.html
    }
}