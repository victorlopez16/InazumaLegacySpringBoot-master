package net.elpuig.inazumalegacy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "equipos_ideales")
public class EquipoIdeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String formacion;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(length = 1000)
    private String jugadoresJson;

    private LocalDateTime creadoEn;

    @PrePersist
    public void prePersist() {
        creadoEn = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getFormacion() { return formacion; }
    public void setFormacion(String formacion) { this.formacion = formacion; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getJugadoresJson() { return jugadoresJson; }
    public void setJugadoresJson(String jugadoresJson) { this.jugadoresJson = jugadoresJson; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}