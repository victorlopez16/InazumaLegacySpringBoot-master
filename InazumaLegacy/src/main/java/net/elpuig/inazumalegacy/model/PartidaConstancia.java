package net.elpuig.inazumalegacy.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_pachangas")
public class PartidaConstancia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jugador_id")
    private Long jugadorId;

    @Column(name = "rival_id")
    private Long rivalId;

    private String resultado; // "VICTORIA" o "DERROTA"

    @Column(name = "fecha_partida")
    private LocalDateTime fechaPartida;

    // Constructor vacío requerido por JPA
    public PartidaConstancia() {
        this.fechaPartida = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getJugadorId() { return jugadorId; }
    public void setJugadorId(Long jugadorId) { this.jugadorId = jugadorId; }

    public Long getRivalId() { return rivalId; }
    public void setRivalId(Long rivalId) { this.rivalId = rivalId; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    public LocalDateTime getFechaPartida() { return fechaPartida; }
    public void setFechaPartida(LocalDateTime fechaPartida) { this.fechaPartida = fechaPartida; }
}