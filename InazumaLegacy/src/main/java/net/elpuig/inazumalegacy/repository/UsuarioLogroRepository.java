package net.elpuig.inazumalegacy.repository;

import net.elpuig.inazumalegacy.model.Logro;
import net.elpuig.inazumalegacy.model.Usuario;
import net.elpuig.inazumalegacy.model.UsuarioLogro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UsuarioLogroRepository extends JpaRepository<UsuarioLogro, Long> {
    boolean existsByUsuarioAndLogro(Usuario usuario, Logro logro);
    List<UsuarioLogro> findByUsuario(Usuario usuario);
}
