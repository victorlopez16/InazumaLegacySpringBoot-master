package net.elpuig.inazumalegacy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seguidores",
        uniqueConstraints = @UniqueConstraint(columnNames = {"seguidor_id", "seguido_id"}))
public class Seguidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seguidor_id", nullable = false)
    private Usuario seguidor;   // quien sigue

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seguido_id", nullable = false)
    private Usuario seguido;    // a quien sigue

    private LocalDateTime fecha;

    @PrePersist
    public void prePersist() { this.fecha = LocalDateTime.now(); }


    public Long getId() { return id; }
    public Usuario getSeguidor() { return seguidor; }
    public void setSeguidor(Usuario seguidor) { this.seguidor = seguidor; }
    public Usuario getSeguido() { return seguido; }
    public void setSeguido(Usuario seguido) { this.seguido = seguido; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}