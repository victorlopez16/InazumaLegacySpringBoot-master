package net.elpuig.inazumalegacy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_logros",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "logro_id"}))
public class UsuarioLogro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logro_id", nullable = false)
    private Logro logro;

    private LocalDateTime fechaDesbloqueo;

    @PrePersist
    public void prePersist() { this.fechaDesbloqueo = LocalDateTime.now(); }


    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Logro getLogro() { return logro; }
    public void setLogro(Logro logro) { this.logro = logro; }
    public LocalDateTime getFechaDesbloqueo() { return fechaDesbloqueo; }
    public void setFechaDesbloqueo(LocalDateTime fechaDesbloqueo) { this.fechaDesbloqueo = fechaDesbloqueo; }
}