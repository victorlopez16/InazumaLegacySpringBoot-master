package net.elpuig.inazumalegacy.model;

import jakarta.persistence.*;

@Entity
@Table(name = "jugadores_raimon_s2")
public class JugadorS2 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String posicion;
    private String equipo;
    private int temporada;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "descripcion")
    private String descripcion;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getPosicion() { return posicion; }
    public void setPosicion(String posicion) { this.posicion = posicion; }
    public String getEquipo() { return equipo; }
    public void setEquipo(String equipo) { this.equipo = equipo; }
    public int getTemporada() { return temporada; }
    public void setTemporada(int temporada) { this.temporada = temporada; }
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}