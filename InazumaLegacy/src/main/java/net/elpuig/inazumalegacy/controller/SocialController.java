package net.elpuig.inazumalegacy.controller;

import jakarta.servlet.http.HttpSession;
import net.elpuig.inazumalegacy.model.*;
import net.elpuig.inazumalegacy.repository.*;
import net.elpuig.inazumalegacy.service.LogroService;
import net.elpuig.inazumalegacy.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/social")
public class SocialController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private SeguidorRepository seguidorRepository;
    @Autowired private NotificacionRepository notificacionRepository;
    @Autowired private ReaccionRepository reaccionRepository;
    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private NotificacionService notificacionService;
    @Autowired private LogroService logroService;
    @Autowired private SimpMessagingTemplate messagingTemplate;


    @PostMapping("/seguir/{nombreSeguido}")
    public ResponseEntity<?> seguir(@PathVariable String nombreSeguido, HttpSession session) {
        String miNombre = (String) session.getAttribute("usuario");
        if (miNombre == null) return ResponseEntity.status(401).build();
        if (miNombre.equals(nombreSeguido)) return ResponseEntity.badRequest().body("No puedes seguirte a ti mismo");

        Usuario yo = usuarioRepository.findByNombre(miNombre).orElseThrow();
        Usuario otro = usuarioRepository.findByNombre(nombreSeguido).orElse(null);
        if (otro == null) return ResponseEntity.notFound().build();

        if (seguidorRepository.existsBySeguidorAndSeguido(yo, otro)) {
            return ResponseEntity.ok(Map.of("estado", "ya_sigues"));
        }

        Seguidor s = new Seguidor();
        s.setSeguidor(yo);
        s.setSeguido(otro);
        seguidorRepository.save(s);

        notificacionService.crear(otro, Notificacion.Tipo.SEGUIDOR, "👥 " + miNombre + " ha comenzado a seguirte");


        logroService.intentarDesbloquear(yo, "SEGUIR_ALGUIEN");
        logroService.verificarLogrosSeguidores(otro);

        return ResponseEntity.ok(Map.of(
                "estado", "siguiendo",
                "seguidores", seguidorRepository.countBySeguido(otro)
        ));
    }

    @DeleteMapping("/seguir/{nombreSeguido}")
    public ResponseEntity<?> dejarDeSeguir(@PathVariable String nombreSeguido, HttpSession session) {
        String miNombre = (String) session.getAttribute("usuario");
        if (miNombre == null) return ResponseEntity.status(401).build();

        Usuario yo = usuarioRepository.findByNombre(miNombre).orElseThrow();
        Usuario otro = usuarioRepository.findByNombre(nombreSeguido).orElse(null);
        if (otro == null) return ResponseEntity.notFound().build();

        seguidorRepository.findBySeguidorAndSeguido(yo, otro).ifPresent(seguidorRepository::delete);

        return ResponseEntity.ok(Map.of(
                "estado", "no_sigues",
                "seguidores", seguidorRepository.countBySeguido(otro)
        ));
    }

    @GetMapping("/seguir/{nombreSeguido}/estado")
    public ResponseEntity<?> estadoSeguimiento(@PathVariable String nombreSeguido, HttpSession session) {
        String miNombre = (String) session.getAttribute("usuario");
        if (miNombre == null) return ResponseEntity.status(401).build();

        Usuario yo = usuarioRepository.findByNombre(miNombre).orElseThrow();
        Usuario otro = usuarioRepository.findByNombre(nombreSeguido).orElse(null);
        if (otro == null) return ResponseEntity.notFound().build();

        boolean sigues = seguidorRepository.existsBySeguidorAndSeguido(yo, otro);
        return ResponseEntity.ok(Map.of(
                "sigues", sigues,
                "seguidores", seguidorRepository.countBySeguido(otro),
                "siguiendo", seguidorRepository.countBySeguidor(otro)
        ));
    }


    @GetMapping("/notificaciones")
    public ResponseEntity<?> obtenerNotificaciones(HttpSession session) {
        String miNombre = (String) session.getAttribute("usuario");
        if (miNombre == null) return ResponseEntity.status(401).build();

        List<Notificacion> lista = notificacionService.obtenerTodas(miNombre);

        List<Map<String, Object>> resultado = lista.stream().map(n -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", n.getId());
            m.put("tipo", n.getTipo().name());
            m.put("mensaje", n.getMensaje());
            m.put("leida", n.isLeida());
            m.put("fecha", n.getFecha().toString());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/notificaciones/count")
    public ResponseEntity<?> contarNoLeidas(HttpSession session) {
        String miNombre = (String) session.getAttribute("usuario");
        if (miNombre == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(Map.of("noLeidas", notificacionService.contarNoLeidas(miNombre)));
    }

    @PostMapping("/notificaciones/leer")
    public ResponseEntity<?> marcarLeidas(HttpSession session) {
        String miNombre = (String) session.getAttribute("usuario");
        if (miNombre == null) return ResponseEntity.status(401).build();
        notificacionService.marcarTodasLeidas(miNombre);
        return ResponseEntity.ok(Map.of("ok", true));
    }


    @GetMapping("/logros")
    public ResponseEntity<?> misLogros(HttpSession session) {
        String miNombre = (String) session.getAttribute("usuario");
        if (miNombre == null) return ResponseEntity.status(401).build();

        Usuario yo = usuarioRepository.findByNombre(miNombre).orElseThrow();
        List<UsuarioLogro> lista = logroService.obtenerLogrosUsuario(yo);

        List<Map<String, Object>> resultado = lista.stream().map(ul -> {
            Map<String, Object> m = new HashMap<>();
            m.put("nombre", ul.getLogro().getNombre());
            m.put("descripcion", ul.getLogro().getDescripcion());
            m.put("icono", ul.getLogro().getIcono());
            m.put("rareza", ul.getLogro().getRareza());
            m.put("fecha", ul.getFechaDesbloqueo().toString());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/reaccion/{mensajeId}/{emoji}")
    public ResponseEntity<?> reaccionar(
            @PathVariable Long mensajeId,
            @PathVariable String emoji,
            HttpSession session) {

        String miNombre = (String) session.getAttribute("usuario");
        if (miNombre == null) return ResponseEntity.status(401).build();

        Usuario yo = usuarioRepository.findByNombre(miNombre).orElseThrow();
        Mensaje mensaje = mensajeRepository.findById(mensajeId).orElse(null);
        if (mensaje == null) return ResponseEntity.notFound().build();

        List<String> emojisPermitidos = List.of("👍", "❤️", "⚡", "😂", "😮");
        if (!emojisPermitidos.contains(emoji)) return ResponseEntity.badRequest().body("Emoji no permitido");

        boolean yaReacciono = reaccionRepository.existsByMensajeAndUsuarioAndEmoji(mensaje, yo, emoji);

        if (yaReacciono) {
            reaccionRepository.findByMensajeAndUsuarioAndEmoji(mensaje, yo, emoji)
                    .ifPresent(reaccionRepository::delete);
        } else {
            Reaccion r = new Reaccion();
            r.setMensaje(mensaje);
            r.setUsuario(yo);
            r.setEmoji(emoji);
            reaccionRepository.save(r);

            logroService.intentarDesbloquear(yo, "PRIMERA_REACCION");

            if (!mensaje.getRemitente().equals(miNombre)) {
                usuarioRepository.findByNombre(mensaje.getRemitente()).ifPresent(autor ->
                        notificacionService.crear(autor, Notificacion.Tipo.REACCION,
                                emoji + " " + miNombre + " reaccionó a tu mensaje")
                );
            }
        }

        List<Object[]> totales = reaccionRepository.contarReaccionesPorMensaje(mensajeId);
        Map<String, Long> resumen = new HashMap<>();
        for (Object[] fila : totales) {
            resumen.put((String) fila[0], (Long) fila[1]);
        }

        Map<String, Object> payload = Map.of(
                "mensajeId", mensajeId,
                "reacciones", resumen
        );

        messagingTemplate.convertAndSend("/topic/reacciones", (Object) payload);

        return ResponseEntity.ok(payload);
    }

    @GetMapping("/reaccion/{mensajeId}")
    public ResponseEntity<?> obtenerReacciones(@PathVariable Long mensajeId) {
        List<Object[]> totales = reaccionRepository.contarReaccionesPorMensaje(mensajeId);
        Map<String, Long> resumen = new LinkedHashMap<>();
        for (Object[] fila : totales) {
            resumen.put((String) fila[0], (Long) fila[1]);
        }
        return ResponseEntity.ok(resumen);
    }
}