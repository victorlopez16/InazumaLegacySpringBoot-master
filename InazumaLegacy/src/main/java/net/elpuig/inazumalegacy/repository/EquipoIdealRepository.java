package net.elpuig.inazumalegacy.repository;

import net.elpuig.inazumalegacy.model.EquipoIdeal;
import net.elpuig.inazumalegacy.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EquipoIdealRepository extends JpaRepository<EquipoIdeal, Long> {
    List<EquipoIdeal> findByUsuario(Usuario usuario);
}