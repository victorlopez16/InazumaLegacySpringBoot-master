package net.elpuig.inazumalegacy.repository;

import net.elpuig.inazumalegacy.model.JugadorS2;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JugadorS2Repository extends JpaRepository<JugadorS2, Long> {
    List<JugadorS2> findAll();
}
