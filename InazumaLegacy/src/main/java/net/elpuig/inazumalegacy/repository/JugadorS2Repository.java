package net.elpuig.inazumalegacy.repository;

import net.elpuig.inazumalegacy.model.JugadorS2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface JugadorS2Repository extends JpaRepository<JugadorS2, Long> {
}