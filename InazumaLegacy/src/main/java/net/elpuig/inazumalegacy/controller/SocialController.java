package net.elpuig.inazumalegacy.controller;

import jakarta.servlet.http.HttpSession;
import net.elpuig.inazumalegacy.model.*;
import net.elpuig.inazumalegacy.repository.*;
import net.elpuig.inazumalegacy.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/social")
public class SocialController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private SeguidorRepository seguidorRepository;
    @Autowired private NotificacionRepository notificacionRepository;
    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private NotificacionService notificacionService;

    // Gestión de Seguidores
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
        s.setSeguidor(otro);
        seguidorRepository.save(s);

        notificacionService.crear(otro, Notificacion.Tipo.SEGUIDOR, "👥 " + miNombre + " ha comenzado a seguirte");

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
}