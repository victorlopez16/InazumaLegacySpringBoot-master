package net.elpuig.inazumalegacy.model;


public class MensajeDTO {

    private String remitente;
    private String destinatario;
    private String contenido;
    private String tipo;
    private String fechaEnvio;

    public String getRemitente() { return remitente; }
    public void setRemitente(String remitente) { this.remitente = remitente; }

    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(String fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public String getAutor() { return remitente; }
    public void setAutor(String autor) { this.remitente = autor; }
}