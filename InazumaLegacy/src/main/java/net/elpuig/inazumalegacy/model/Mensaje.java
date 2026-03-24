package net.elpuig.inazumalegacy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Mensaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String remitente;
    private String destinatario; // "GLOBAL", "IA", o nombre de usuario

    @Column(columnDefinition = "TEXT")
    private String contenido; // Aquí irá el texto o la cadena Base64 si es audio

    private String tipo; // "TEXTO", "AUDIO"
    private LocalDateTime fechaEnvio;

    @PrePersist
    public void prePersist() {
        this.fechaEnvio = LocalDateTime.now();
    }
}