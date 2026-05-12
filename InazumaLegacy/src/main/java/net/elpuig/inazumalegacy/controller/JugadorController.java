package net.elpuig.inazumalegacy.controller;

import jakarta.servlet.http.HttpSession;
import net.elpuig.inazumalegacy.model.Jugador;
import net.elpuig.inazumalegacy.model.JugadorS2;
import net.elpuig.inazumalegacy.model.JugadorS3;
import net.elpuig.inazumalegacy.model.PartidaConstancia; // Nueva entidad
import net.elpuig.inazumalegacy.repository.JugadorRepository;
import net.elpuig.inazumalegacy.repository.JugadorS2Repository;
import net.elpuig.inazumalegacy.repository.JugadorS3Repository;
import net.elpuig.inazumalegacy.repository.PartidaConstanciaRepository; // Nuevo repo
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

@Controller
public class JugadorController {

    @Autowired
    private JugadorRepository jugadorRepo;

    @Autowired
    private JugadorS2Repository jugadorS2Repo;

    @Autowired
    private JugadorS3Repository jugadorS3Repo;

    @Autowired
    private PartidaConstanciaRepository constanciaRepo; // Repositorio para el guardado silencioso

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
    public String jugarPachanga(@RequestParam Long idJugador, Model model, HttpSession session) {

        // 1. Lógica de descanso (Cooldown de 1 minuto)
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime ultimaPachanga = (LocalDateTime) session.getAttribute("ultima_pachanga_time");

        if (ultimaPachanga != null) {
            long segundosTranscurridos = Duration.between(ultimaPachanga, ahora).getSeconds();
            if (segundosTranscurridos < 60) {
                long falta = 60 - segundosTranscurridos;
                model.addAttribute("error", "Tus jugadores están agotados. Debes esperar " + falta + " segundos.");
                return "pachanga-espera"; // Redirige a la vista de espera
            }
        }

        // 2. Lógica de la pachanga
        Jugador miJugador = jugadorRepo.findById(idJugador).orElseThrow();
        Jugador rival = jugadorRepo.findRandomRival(idJugador);

        int miPoder = miJugador.getAtaque() + (int)(Math.random() * 21);
        int rivalPoder = rival.getDefensa() + (int)(Math.random() * 21);
        boolean victoria = miPoder > rivalPoder;

        if (victoria) {
            miJugador.setVictorias(miJugador.getVictorias() + 1);
            miJugador.setPuntosInazuma(miJugador.getPuntosInazuma() + 100);
            model.addAttribute("mensaje", "¡VICTORIA!");
        } else {
            model.addAttribute("mensaje", "DERROTA...");
        }

        // Guardar progreso del jugador
        jugadorRepo.save(miJugador);

        // 3. Guardado silencioso en la BD (Constancia)
        PartidaConstancia registro = new PartidaConstancia();
        registro.setJugadorId(idJugador);
        registro.setRivalId(rival.getId());
        registro.setResultado(victoria ? "VICTORIA" : "DERROTA");
        constanciaRepo.save(registro); // Solo queda en la BD, no hay historial en la web

        // 4. Actualizar el tiempo en la sesión
        session.setAttribute("ultima_pachanga_time", ahora);

        model.addAttribute("jugador", miJugador);
        model.addAttribute("rival", rival);
        model.addAttribute("miPoder", miPoder);
        model.addAttribute("rivalPoder", rivalPoder);
        model.addAttribute("victoria", victoria);

        return "pachanga-resultado";
    }
}