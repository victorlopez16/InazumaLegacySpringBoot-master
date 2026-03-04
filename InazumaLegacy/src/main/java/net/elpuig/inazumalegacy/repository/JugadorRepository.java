package net.elpuig.inazumalegacy.repository;

import net.elpuig.inazumalegacy.model.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {
    List<Jugador> findByTemporada(int temporada);
}
