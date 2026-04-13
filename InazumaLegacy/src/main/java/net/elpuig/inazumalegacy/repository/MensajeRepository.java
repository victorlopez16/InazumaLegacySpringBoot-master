package net.elpuig.inazumalegacy.repository;

import net.elpuig.inazumalegacy.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    // Método original (sin filtrar)
    List<Mensaje> findTop50ByOrderByFechaEnvioAsc();

    // BUG 2 CORREGIDO: método necesario para cargar historial global en /chat
    List<Mensaje> findTop50ByDestinatarioOrderByFechaEnvioAsc(String destinatario);
}