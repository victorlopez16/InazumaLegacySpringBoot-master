package net.elpuig.inazumalegacy.model;

import jakarta.persistence.*;

@Entity
@Table(name = "jugadores_raimon_s1")
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String posicion;
    private String equipo;
    private String afiliacion;
    private int temporada;

    private int ataque;
    private int defensa;
    private int victorias;

    @Column(name = "puntos_inazuma")
    private int puntosInazuma;

    @Column(name = "foto_url")
    private String fotoUrl;

    private String descripcion;

    public double getScoreTotal() {
        return (puntosInazuma * 10) + (ataque * 5) + (defensa * 5) + (victorias * 50);
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPosicion() { return posicion; }
    public void setPosicion(String posicion) { this.posicion = posicion; }

    public String getEquipo() { return equipo; }
    public void setEquipo(String equipo) { this.equipo = equipo; }

    public String getAfiliacion() { return afiliacion; }
    public void setAfiliacion(String afiliacion) { this.afiliacion = afiliacion; }

    public int getTemporada() { return temporada; }
    public void setTemporada(int temporada) { this.temporada = temporada; }

    public int getAtaque() { return ataque; }
    public void setAtaque(int ataque) { this.ataque = ataque; }

    public int getDefensa() { return defensa; }
    public void setDefensa(int defensa) { this.defensa = defensa; }

    public int getVictorias() { return victorias; }
    public void setVictorias(int victorias) { this.victorias = victorias; }

    public int getPuntosInazuma() { return puntosInazuma; }
    public void setPuntosInazuma(int puntosInazuma) { this.puntosInazuma = puntosInazuma; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}