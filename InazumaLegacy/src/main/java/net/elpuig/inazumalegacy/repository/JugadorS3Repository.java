package net.elpuig.inazumalegacy.repository;

import net.elpuig.inazumalegacy.model.JugadorS3;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JugadorS3Repository extends JpaRepository<JugadorS3, Long> {
}