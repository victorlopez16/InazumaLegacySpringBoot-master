package net.elpuig.inazumalegacy.repository;

import net.elpuig.inazumalegacy.model.PartidaConstancia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartidaConstanciaRepository extends JpaRepository<PartidaConstancia, Long> {
}