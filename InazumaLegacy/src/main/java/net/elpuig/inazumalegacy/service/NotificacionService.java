package net.elpuig.inazumalegacy.service;

import net.elpuig.inazumalegacy.model.Notificacion;
import net.elpuig.inazumalegacy.model.Usuario;
import net.elpuig.inazumalegacy.repository.NotificacionRepository;
import net.elpuig.inazumalegacy.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    public void crear(Usuario destinatario, Notificacion.Tipo tipo, String mensaje) {
        Notificacion n = new Notificacion();
        n.setUsuario(destinatario);
        n.setTipo(tipo);
        n.setMensaje(mensaje);
        notificacionRepository.save(n);

        long noLeidas = notificacionRepository.countByUsuarioAndLeidaFalse(destinatario);
        messagingTemplate.convertAndSend(
                "/topic/notif." + destinatario.getNombre(),
                (Object) Map.of("mensaje", mensaje,
                        "tipo", tipo.name(),
                        "noLeidas", noLeidas)
        );
    }
    public List<Notificacion> obtenerTodas(String nombreUsuario) {
        Usuario u = usuarioRepository.findByNombre(nombreUsuario).orElseThrow();
        return notificacionRepository.findByUsuarioOrderByFechaDesc(u);
    }

    public long contarNoLeidas(String nombreUsuario) {
        Usuario u = usuarioRepository.findByNombre(nombreUsuario).orElseThrow();
        return notificacionRepository.countByUsuarioAndLeidaFalse(u);
    }

    public void marcarTodasLeidas(String nombreUsuario) {
        Usuario u = usuarioRepository.findByNombre(nombreUsuario).orElseThrow();
        notificacionRepository.marcarTodasLeidas(u);
    }
}