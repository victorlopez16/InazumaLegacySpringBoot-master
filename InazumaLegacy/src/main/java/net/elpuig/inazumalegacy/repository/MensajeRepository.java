package net.elpuig.inazumalegacy.repository;

import net.elpuig.inazumalegacy.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    // Cambia 'Fecha' por 'FechaEnvio'
    List<Mensaje> findTop50ByOrderByFechaEnvioAsc();
}
