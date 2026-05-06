package net.elpuig.inazumalegacy.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "jugadores_raimon_s3")
public class JugadorS3 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String posicion;
    private String equipo;
    private String afiliacion;
    private int ataque;
    private int defensa;
    private int victorias;
    private int temporada;

    @Column(name = "puntos_inazuma")
    private int puntosInazuma;

    private String descripcion;

    @Column(name = "foto_url")
    private String fotoUrl;
}