package net.elpuig.inazumalegacy.controller;

import jakarta.servlet.http.HttpSession;
import net.elpuig.inazumalegacy.model.Jugador;
import net.elpuig.inazumalegacy.model.JugadorS2;
import net.elpuig.inazumalegacy.model.JugadorS3;
import net.elpuig.inazumalegacy.model.Usuario;
import net.elpuig.inazumalegacy.repository.JugadorRepository;
import net.elpuig.inazumalegacy.repository.JugadorS2Repository;
import net.elpuig.inazumalegacy.repository.JugadorS3Repository;
import net.elpuig.inazumalegacy.repository.SeguidorRepository;
import net.elpuig.inazumalegacy.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class JugadorController {

    @Autowired private JugadorRepository jugadorRepo;
    @Autowired private JugadorS2Repository jugadorS2Repo;
    @Autowired private JugadorS3Repository jugadorS3Repo;
    @Autowired private SeguidorRepository seguidorRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @GetMapping("/inicio")
    public String inicio(HttpSession session, Model model) {
        String nombreUsuario = (String) session.getAttribute("usuario");
        if (nombreUsuario == null) return "redirect:/login";

        model.addAttribute("nombreUsuario", nombreUsuario);

        Usuario yo = usuarioRepository.findByNombre(nombreUsuario).orElseThrow();

        List<String> siguiendo = seguidorRepository.findBySeguidor(yo)
                .stream().map(s -> s.getSeguido().getNombre()).toList();

        List<Usuario> sugerencias = usuarioRepository.findAll().stream()
                .filter(u -> !u.getNombre().equals(nombreUsuario))
                .filter(u -> !siguiendo.contains(u.getNombre()))
                .limit(6)
                .toList();

        model.addAttribute("sugerencias", sugerencias);

        return "inicio";
    }

    @GetMapping("/jugador/{id}")
    public String detalle(@PathVariable Long id, @RequestParam int temporada, Model model) {
        if (temporada == 2) {
            model.addAttribute("jugador", jugadorS2Repo.findById(id).orElse(null));
        } else if (temporada == 3) {
            model.addAttribute("jugador", jugadorS3Repo.findById(id).orElse(null));
        } else {
            model.addAttribute("jugador", jugadorRepo.findById(id).orElse(null));
        }
        model.addAttribute("temporada", temporada);
        return "jugador-detalle";
    }

    @GetMapping("/jugadores")
    public String jugadores(@RequestParam(defaultValue = "1") int temporada, Model model) {
        if (temporada == 2) {
            List<JugadorS2> lista = jugadorS2Repo.findAll();
            model.addAttribute("jugadores", lista);
        } else if (temporada == 3) {
            List<JugadorS3> lista = jugadorS3Repo.findAll();
            model.addAttribute("jugadores", lista);
        } else {
            List<Jugador> lista = jugadorRepo.findAll();
            model.addAttribute("jugadores", lista);
        }
        model.addAttribute("temporada", temporada);
        return "jugadores";
    }
}