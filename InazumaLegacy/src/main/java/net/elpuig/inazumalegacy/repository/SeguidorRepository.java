package net.elpuig.inazumalegacy.repository;

import net.elpuig.inazumalegacy.model.Seguidor;
import net.elpuig.inazumalegacy.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SeguidorRepository extends JpaRepository<Seguidor, Long> {

    boolean existsBySeguidorAndSeguido(Usuario seguidor, Usuario seguido);
    Optional<Seguidor> findBySeguidorAndSeguido(Usuario seguidor, Usuario seguido);
    long countBySeguido(Usuario seguido);
    long countBySeguidor(Usuario seguidor);

    List<Seguidor> findBySeguidor(Usuario seguidor);
}