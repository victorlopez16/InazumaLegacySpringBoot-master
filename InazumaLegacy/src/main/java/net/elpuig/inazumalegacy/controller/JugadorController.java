package net.elpuig.inazumalegacy.controller;

import net.elpuig.inazumalegacy.model.Jugador;
import net.elpuig.inazumalegacy.repository.JugadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Controller
public class JugadorController {

    @Autowired
    private JugadorRepository jugadorRepo;

    @GetMapping("/pachanga")
    public String mostrarMenuPachanga(Model model) {
        model.addAttribute("misJugadores", jugadorRepo.findAll());
        return "pachanga-menu";
    }

    @GetMapping("/ranking")
    public String verRanking(Model model) {
        List<Jugador> lista = jugadorRepo.findAllByOrderByPuntosInazumaDesc();
        model.addAttribute("ranking", lista);
        return "ranking";
    }

    @PostMapping("/pachanga/jugar")
    @Transactional
    public String jugarPachanga(@RequestParam Long idJugador, Model model) {
        Jugador miJugador = jugadorRepo.findById(idJugador).orElseThrow();
        Jugador rival = jugadorRepo.findRandomRival(idJugador);

        int miPoder = miJugador.getAtaque() + (int)(Math.random() * 21);
        int rivalPoder = rival.getDefensa() + (int)(Math.random() * 21);

        if (miPoder > rivalPoder) {
            miJugador.setVictorias(miJugador.getVictorias() + 1);
            miJugador.setPuntosInazuma(miJugador.getPuntosInazuma() + 100);
            model.addAttribute("mensaje", "¡VICTORIA!");
        } else {
            model.addAttribute("mensaje", "DERROTA...");
        }

        jugadorRepo.save(miJugador);

        model.addAttribute("jugador", miJugador);
        model.addAttribute("rival", rival);
        model.addAttribute("miPoder", miPoder);
        model.addAttribute("rivalPoder", rivalPoder);
        model.addAttribute("victoria", miPoder > rivalPoder);

        return "pachanga-resultado";
    }
}