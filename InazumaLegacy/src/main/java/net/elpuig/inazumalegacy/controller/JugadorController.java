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
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Controller
public class JugadorController {

    @Autowired
    private JugadorRepository jugadorRepo;

    @Autowired
    private JugadorS2Repository jugadorS2Repo;

    @Autowired
    private JugadorS3Repository jugadorS3Repo;

    @GetMapping("/jugadores")
    public String listarJugadores(@RequestParam(name = "temporada", required = false, defaultValue = "1") int temporada, Model model) {
        if (temporada == 2) {
            model.addAttribute("jugadores", jugadorS2Repo.findAll());
        } else if (temporada == 3) {
            model.addAttribute("jugadores", jugadorS3Repo.findAll());
        } else {
            model.addAttribute("jugadores", jugadorRepo.findAll());
        }

        model.addAttribute("temporadaActual", temporada);
        return "jugadores";
    }

    @GetMapping("/jugador/{id}")
    public String verFicha(@PathVariable Long id, @RequestParam(name = "temporada", defaultValue = "1") int temporada, Model model) {

        if (temporada == 2) {
            JugadorS2 j2 = jugadorS2Repo.findById(id).orElseThrow();
            model.addAttribute("jugador", j2);
        } else if (temporada == 3) {
            JugadorS3 j3 = jugadorS3Repo.findById(id).orElseThrow();
            model.addAttribute("jugador", j3);
        } else {
            Jugador j1 = jugadorRepo.findById(id).orElseThrow();
            model.addAttribute("jugador", j1);
        }

        model.addAttribute("temporada", temporada);

        return "jugador-detalle";
    }

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