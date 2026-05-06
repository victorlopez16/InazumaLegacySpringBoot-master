package net.elpuig.inazumalegacy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_usuario", unique = true)
    private String nombre;

    @Column(unique = true)
    private String email;

    private String password;
    private String descripcion;
    private String afiliacion;
    private String rango;

    private boolean online = false;

    @Column(name = "puntos_inazuma")
    private Integer puntosInazuma = 0;

    private Integer ataque = 10;
    private Integer defensa = 10;
    private Integer rapidez = 10;
    private Integer victorias = 0;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    // Getters y Setters
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getAfiliacion() { return afiliacion; }
    public void setAfiliacion(String afiliacion) { this.afiliacion = afiliacion; }
    public String getRango() { return rango; }
    public void setRango(String rango) { this.rango = rango; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }
    public Integer getPuntosInazuma() { return puntosInazuma; }
    public void setPuntosInazuma(Integer puntosInazuma) { this.puntosInazuma = puntosInazuma; }
    public Integer getAtaque() { return ataque; }
    public void setAtaque(Integer ataque) { this.ataque = ataque; }
    public Integer getDefensa() { return defensa; }
    public void setDefensa(Integer defensa) { this.defensa = defensa; }
    public Integer getRapidez() { return rapidez; }
    public void setRapidez(Integer rapidez) { this.rapidez = rapidez; }
    public Integer getVictorias() { return victorias; }
    public void setVictorias(Integer victorias) { this.victorias = victorias; }
}