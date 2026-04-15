package net.elpuig.inazumalegacy.repository;

import net.elpuig.inazumalegacy.model.Mensaje;
import net.elpuig.inazumalegacy.model.Reaccion;
import net.elpuig.inazumalegacy.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReaccionRepository extends JpaRepository<Reaccion, Long> {

    boolean existsByMensajeAndUsuarioAndEmoji(Mensaje mensaje, Usuario usuario, String emoji);

    Optional<Reaccion> findByMensajeAndUsuarioAndEmoji(Mensaje mensaje, Usuario usuario, String emoji);

    @Query("SELECT r.emoji, COUNT(r) FROM Reaccion r WHERE r.mensaje.id = :mensajeId GROUP BY r.emoji")
    List<Object[]> contarReaccionesPorMensaje(Long mensajeId);

    List<Reaccion> findByMensaje(Mensaje mensaje);
}