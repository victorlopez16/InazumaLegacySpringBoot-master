package net.elpuig.inazumalegacy.repository;

import net.elpuig.inazumalegacy.model.Notificacion;
import net.elpuig.inazumalegacy.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioOrderByFechaDesc(Usuario usuario);

    List<Notificacion> findByUsuarioAndLeidaFalseOrderByFechaDesc(Usuario usuario);

    long countByUsuarioAndLeidaFalse(Usuario usuario);

    @Modifying
    @Transactional
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.usuario = :usuario")
    void marcarTodasLeidas(Usuario usuario);
}