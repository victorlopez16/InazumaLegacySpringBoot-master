package net.elpuig.inazumalegacy.controller;

import net.elpuig.inazumalegacy.model.Jugador;
import net.elpuig.inazumalegacy.model.JugadorS2;
import net.elpuig.inazumalegacy.model.JugadorS3;
import net.elpuig.inazumalegacy.repository.JugadorRepository;
import net.elpuig.inazumalegacy.repository.JugadorS2Repository;
import net.elpuig.inazumalegacy.repository.JugadorS3Repository;
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

    @GetMapping("/inicio")
    public String inicio() { return "inicio"; }

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
