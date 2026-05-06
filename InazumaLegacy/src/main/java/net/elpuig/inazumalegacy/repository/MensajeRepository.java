package net.elpuig.inazumalegacy.repository;

import net.elpuig.inazumalegacy.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {


    List<Mensaje> findTop50ByDestinatarioOrderByFechaEnvioAsc(String destinatario);

    long countByDestinatarioAndLeidoFalse(String destinatario);

    @Query("SELECT m FROM Mensaje m WHERE (m.remitente = :u1 AND m.destinatario = :u2) " +
            "OR (m.remitente = :u2 AND m.destinatario = :u1) ORDER BY m.fechaEnvio ASC")
    List<Mensaje> findPrivateChat(@Param("u1") String u1, @Param("u2") String u2);

    long countByRemitenteAndDestinatario(String remitente, String destinatario);
}