package net.elpuig.inazumalegacy.controller;

import jakarta.servlet.http.HttpSession;
import net.elpuig.inazumalegacy.model.Usuario;
import net.elpuig.inazumalegacy.repository.UsuarioRepository;
import net.elpuig.inazumalegacy.repository.MensajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private MensajeRepository mensajeRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String root(HttpSession session) {
        String nombreUsuario = (String) session.getAttribute("usuario");

        if (nombreUsuario != null) {
            return "redirect:/inicio";
        }

        // Si no hay sesión, le mostramos la vista de invitado
        return "inicio_invitado";
    }

    @GetMapping("/registro")
    public String registroForm() {
        return "registro";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/registro")
    public String registrar(@RequestParam String nombre,
                            @RequestParam String email,
                            @RequestParam String password) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(password));
        u.setOnline(false);
        repo.save(u);
        return "redirect:/login";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@RequestParam String nombre,
                                   @RequestParam String rango,
                                   @RequestParam String afiliacion,
                                   @RequestParam String descripcion,
                                   HttpSession session) {

        String nombreActualSesion = (String) session.getAttribute("usuario");
        if (nombreActualSesion == null) return "redirect:/login";

        Optional<Usuario> oUsuario = repo.findByNombre(nombreActualSesion);

        if (oUsuario.isPresent()) {
            Usuario u = oUsuario.get();

            u.setNombre(nombre);
            u.setRango(rango);
            u.setAfiliacion(afiliacion);
            u.setDescripcion(descripcion);

            repo.save(u);

            session.setAttribute("usuario", nombre);
        }

        return "redirect:/perfil?success";
    }

    @PostMapping("/login")
    public String loginPost(@RequestParam String username,
                            @RequestParam String password,
                            HttpSession session) {
        Optional<Usuario> usuario = repo.findByNombre(username);
        if (usuario.isPresent() && passwordEncoder.matches(password, usuario.get().getPassword())) {
            session.setAttribute("usuario", usuario.get().getNombre());

            Usuario u = usuario.get();
            u.setOnline(true);
            repo.save(u);

            return "redirect:/inicio";
        }
        return "redirect:/login?error";
    }

    @GetMapping("/inicio")
    public String inicio(HttpSession session, Model model) {
        String nombreUsuario = (String) session.getAttribute("usuario");

        if (nombreUsuario == null) return "redirect:/login";

        Optional<Usuario> usuarioOpt = repo.findByNombre(nombreUsuario);

        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            model.addAttribute("user", u);
            model.addAttribute("nombreUsuario", u.getNombre());

            long noLeidos = mensajeRepo.countByDestinatarioAndLeidoFalse(u.getNombre());
            model.addAttribute("mensajesNuevos", noLeidos);
        } else {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute("todosLosUsuarios", repo.findAll());

        return "inicio";
    }

    @GetMapping("/perfil")
    public String perfil(HttpSession session, Model model) {
        String nombreUsuario = (String) session.getAttribute("usuario");
        if (nombreUsuario == null) return "redirect:/login";

        repo.findByNombre(nombreUsuario).ifPresent(u -> model.addAttribute("user", u));
        return "perfil";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        String nombreUsuario = (String) session.getAttribute("usuario");
        if (nombreUsuario != null) {
            repo.findByNombre(nombreUsuario).ifPresent(u -> {
                u.setOnline(false);
                repo.save(u);
            });
        }
        session.invalidate();
        return "redirect:/";
    }
}