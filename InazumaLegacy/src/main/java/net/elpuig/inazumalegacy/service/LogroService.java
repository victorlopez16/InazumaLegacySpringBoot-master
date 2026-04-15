package net.elpuig.inazumalegacy.service;

import jakarta.annotation.PostConstruct;
import net.elpuig.inazumalegacy.model.*;
import net.elpuig.inazumalegacy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogroService {

    @Autowired private LogroRepository logroRepository;
    @Autowired private UsuarioLogroRepository usuarioLogroRepository;
    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private SeguidorRepository seguidorRepository;
    @Autowired private NotificacionService notificacionService;

    @PostConstruct
    public void inicializarLogros() {
        crearSiNoExiste("PRIMER_MENSAJE",   "Primer Contacto",       "Envía tu primer mensaje en el chat global",    "⚡", "COMUN");
        crearSiNoExiste("MENSAJES_10",      "Comunicador",           "Envía 10 mensajes en el chat global",          "💬", "COMUN");
        crearSiNoExiste("MENSAJES_50",      "Veterano del Chat",     "Envía 50 mensajes en el chat global",          "🔥", "RARO");
        crearSiNoExiste("MENSAJES_100",     "Leyenda del Chat",      "Envía 100 mensajes en el chat global",         "👑", "EPICO");
        crearSiNoExiste("PRIMER_SEGUIDOR",  "Influencer Emergente",  "Consigue tu primer seguidor",                  "🌟", "COMUN");
        crearSiNoExiste("SEGUIDORES_5",     "Popular en Raimon",     "Consigue 5 seguidores",                        "🏆", "RARO");
        crearSiNoExiste("SEGUIR_ALGUIEN",   "Explorador Social",     "Sigue a otro jugador por primera vez",         "👥", "COMUN");
        crearSiNoExiste("PRIMERA_REACCION", "Expresivo",             "Reacciona a un mensaje por primera vez",       "😄", "COMUN");
        crearSiNoExiste("CONSULTA_IA",      "Táctico Digital",       "Consulta a la IA por primera vez",             "🤖", "RARO");
        crearSiNoExiste("CONSULTAS_IA_10",  "Estratega Supremo",     "Realiza 10 consultas a la IA",                 "🧠", "EPICO");
    }

    private void crearSiNoExiste(String codigo, String nombre, String desc, String icono, String rareza) {
        if (logroRepository.findByCodigo(codigo).isEmpty()) {
            Logro l = new Logro();
            l.setCodigo(codigo);
            l.setNombre(nombre);
            l.setDescripcion(desc);
            l.setIcono(icono);
            l.setRareza(rareza);
            logroRepository.save(l);
        }
    }

    public void intentarDesbloquear(Usuario usuario, String codigo) {
        logroRepository.findByCodigo(codigo).ifPresent(logro -> {
            if (!usuarioLogroRepository.existsByUsuarioAndLogro(usuario, logro)) {
                UsuarioLogro ul = new UsuarioLogro();
                ul.setUsuario(usuario);
                ul.setLogro(logro);
                usuarioLogroRepository.save(ul);

                // Notificación automática del logro
                notificacionService.crear(
                        usuario,
                        Notificacion.Tipo.LOGRO,
                        logro.getIcono() + " ¡Logro desbloqueado! «" + logro.getNombre() + "» — " + logro.getDescripcion()
                );
            }
        });
    }

    public void verificarLogrosMensaje(Usuario usuario) {
        long total = mensajeRepository.countByRemitenteAndDestinatario(usuario.getNombre(), "GLOBAL");

        if (total >= 1)   intentarDesbloquear(usuario, "PRIMER_MENSAJE");
        if (total >= 10)  intentarDesbloquear(usuario, "MENSAJES_10");
        if (total >= 50)  intentarDesbloquear(usuario, "MENSAJES_50");
        if (total >= 100) intentarDesbloquear(usuario, "MENSAJES_100");
    }

    public void verificarLogrosIA(Usuario usuario) {
        long totalIA = mensajeRepository.countByRemitenteAndDestinatario(usuario.getNombre(), "IA");

        if (totalIA >= 1)  intentarDesbloquear(usuario, "CONSULTA_IA");
        if (totalIA >= 10) intentarDesbloquear(usuario, "CONSULTAS_IA_10");
    }

    public void verificarLogrosSeguidores(Usuario seguido) {
        long seguidores = seguidorRepository.countBySeguido(seguido);
        if (seguidores >= 1) intentarDesbloquear(seguido, "PRIMER_SEGUIDOR");
        if (seguidores >= 5) intentarDesbloquear(seguido, "SEGUIDORES_5");
    }

    public List<UsuarioLogro> obtenerLogrosUsuario(Usuario usuario) {
        return usuarioLogroRepository.findByUsuario(usuario);
    }
}