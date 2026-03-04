package net.elpuig.inazumalegacy.model;

import jakarta.persistence.*;

@Entity
@Table(name = "jugadores_raimon_s2")
public class JugadorS2 {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String posicion;
    private String equipo;
    private int temporada;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getPosicion() { return posicion; }
    public String getEquipo() { return equipo; }
    public int getTemporada() { return temporada; }
}
