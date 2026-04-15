package net.elpuig.inazumalegacy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reacciones",
        uniqueConstraints = @UniqueConstraint(columnNames = {"mensaje_id", "usuario_id", "emoji"}))
public class Reaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mensaje_id", nullable = false)
    private Mensaje mensaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    private String emoji;

    private LocalDateTime fecha;

    @PrePersist
    public void prePersist() { this.fecha = LocalDateTime.now(); }


    public Long getId() { return id; }
    public Mensaje getMensaje() { return mensaje; }
    public void setMensaje(Mensaje mensaje) { this.mensaje = mensaje; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}