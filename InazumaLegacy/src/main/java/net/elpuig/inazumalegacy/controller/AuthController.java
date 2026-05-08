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
    public String root() {
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

    // --- MÉTODO ACTUALIZADO PARA QUE FUNCIONEN TODOS LOS CAMPOS ---
    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@RequestParam String nombre,
                                   @RequestParam String rango,
                                   @RequestParam String afiliacion,
                                   @RequestParam String descripcion,
                                   HttpSession session) {

        // 1. Obtener el nombre actual de la sesión para buscar al usuario
        String nombreActualSesion = (String) session.getAttribute("usuario");
        if (nombreActualSesion == null) return "redirect:/login";

        Optional<Usuario> oUsuario = repo.findByNombre(nombreActualSesion);

        if (oUsuario.isPresent()) {
            Usuario u = oUsuario.get();

            // 2. Actualizar todos los campos con los @RequestParam
            u.setNombre(nombre);
            u.setRango(rango);
            u.setAfiliacion(afiliacion);
            u.setDescripcion(descripcion);

            // 3. Guardar cambios en la base de datos
            repo.save(u);

            // 4. MUY IMPORTANTE: Actualizar el nombre en la sesión por si ha cambiado
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

        // Seguridad: Si no hay sesión, fuera.
        if (nombreUsuario == null) return "redirect:/login";

        // Buscamos al usuario logueado
        Optional<Usuario> usuarioOpt = repo.findByNombre(nombreUsuario);

        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            model.addAttribute("user", u);
            model.addAttribute("nombreUsuario", u.getNombre());

            // Conteo de mensajes
            long noLeidos = mensajeRepo.countByDestinatarioAndLeidoFalse(u.getNombre());
            model.addAttribute("mensajesNuevos", noLeidos);
        } else {
            // Si por algún motivo el usuario de la sesión no existe en DB, limpiamos
            session.invalidate();
            return "redirect:/login";
        }

        // CARGA DE LA LISTA PARA EL SIDEBAR
        // Importante: .findAll() nunca devuelve null, devuelve lista vacía si no hay nadie.
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