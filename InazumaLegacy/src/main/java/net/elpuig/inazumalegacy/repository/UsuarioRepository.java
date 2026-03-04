package net.elpuig.inazumalegacy.repository;

import net.elpuig.inazumalegacy.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByNombre(String nombre);
    Optional<Usuario> findByNombreOrEmail(String nombre, String email);
}
