package net.elpuig.inazumalegacy.controller;

import net.elpuig.inazumalegacy.model.*;
import net.elpuig.inazumalegacy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/equipo-ideal")
public class EquipoIdealController {

    @Autowired private JugadorRepository jugadorRepo;
    @Autowired private JugadorS2Repository jugadorS2Repo;
    @Autowired private JugadorS3Repository jugadorS3Repo;
    @Autowired private EquipoIdealRepository equipoIdealRepo;
    @Autowired private UsuarioRepository usuarioRepo;

    @GetMapping
    public String mostrarCreador(Model model, Authentication auth) {
        List<Map<String, Object>> todos = new ArrayList<>();

        jugadorRepo.findAll().forEach(j -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", "S1-" + j.getId());
            m.put("nombre", j.getNombre());
            m.put("posicion", j.getPosicion());
            m.put("equipo", j.getEquipo());
            m.put("temporada", "S1");
            todos.add(m);
        });

        jugadorS2Repo.findAll().forEach(j -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", "S2-" + j.getId());
            m.put("nombre", j.getNombre());
            m.put("posicion", j.getPosicion());
            m.put("equipo", j.getEquipo());
            m.put("temporada", "S2");
            todos.add(m);
        });

        jugadorS3Repo.findAll().forEach(j -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", "S3-" + j.getId());
            m.put("nombre", j.getNombre());
            m.put("posicion", j.getPosicion());
            m.put("equipo", j.getEquipo());
            m.put("temporada", "S3");
            todos.add(m);
        });

        List<EquipoIdeal> misEquipos = new ArrayList<>();
        if (auth != null) {
            usuarioRepo.findByNombre(auth.getName())
                    .ifPresent(u -> misEquipos.addAll(equipoIdealRepo.findByUsuario(u)));
        }

        model.addAttribute("jugadores", todos);
        model.addAttribute("misEquipos", misEquipos);
        model.addAttribute("formaciones", List.of("4-3-3","4-4-2","3-4-3","3-5-2","5-3-2","4-5-1"));
        return "equipo-ideal";
    }

    @PostMapping("/guardar")
    public String guardarEquipo(
            @RequestParam String nombre,
            @RequestParam String formacion,
            @RequestParam String jugadoresJson,
            Authentication auth,
            RedirectAttributes ra) {

        if (auth == null) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para guardar.");
            return "redirect:/equipo-ideal";
        }

        Optional<Usuario> usuarioOpt = usuarioRepo.findByNombre(auth.getName());
        if (usuarioOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Usuario no encontrado.");
            return "redirect:/equipo-ideal";
        }

        EquipoIdeal eq = new EquipoIdeal();
        eq.setNombre(nombre.isBlank() ? "Mi 11 ideal" : nombre);
        eq.setFormacion(formacion);
        eq.setJugadoresJson(jugadoresJson);
        eq.setUsuario(usuarioOpt.get());
        equipoIdealRepo.save(eq);

        ra.addFlashAttribute("exito", "¡Equipo guardado correctamente!");
        return "redirect:/equipo-ideal";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarEquipo(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        equipoIdealRepo.findById(id).ifPresent(eq -> {
            if (auth != null && eq.getUsuario().getNombre().equals(auth.getName())) {
                equipoIdealRepo.delete(eq);
                ra.addFlashAttribute("exito", "Equipo eliminado.");
            }
        });
        return "redirect:/equipo-ideal";
    }
}