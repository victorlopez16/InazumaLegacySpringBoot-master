package net.elpuig.inazumalegacy.repository;

import net.elpuig.inazumalegacy.model.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {

    List<Jugador> findByTemporada(int temporada);

    List<Jugador> findAllByOrderByPuntosInazumaDesc();

    @Query(value = "SELECT * FROM jugadores_raimon_s1 WHERE id != :idJugador ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Jugador findRandomRival(@Param("idJugador") Long idJugador);
}