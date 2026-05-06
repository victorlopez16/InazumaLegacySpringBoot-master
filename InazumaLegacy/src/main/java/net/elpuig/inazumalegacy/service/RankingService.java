package net.elpuig.inazumalegacy.service;

import net.elpuig.inazumalegacy.model.Jugador;
import net.elpuig.inazumalegacy.repository.JugadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RankingService {

    @Autowired
    private JugadorRepository jugadorRepository;

    public void entrenarJugador(Long id, int subirAtaque, int subirDefensa) {
        Jugador j = jugadorRepository.findById(id).orElse(null);
        if (j != null) {
            j.setAtaque(j.getAtaque() + subirAtaque);
            j.setDefensa(j.getDefensa() + subirDefensa);
            jugadorRepository.save(j);
        }
    }

    public void registrarVictoria(Long id) {
        Jugador j = jugadorRepository.findById(id).orElse(null);
        if (j != null) {
            j.setVictorias(j.getVictorias() + 1);
            j.setPuntosInazuma(j.getPuntosInazuma() + 100);
            jugadorRepository.save(j);
        }
    }
}