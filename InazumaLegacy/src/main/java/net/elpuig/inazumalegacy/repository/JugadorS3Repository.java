package net.elpuig.inazumalegacy.repository;

import net.elpuig.inazumalegacy.model.JugadorS3;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JugadorS3Repository extends JpaRepository<JugadorS3, Long> {
    List<JugadorS3> findAll();
}
