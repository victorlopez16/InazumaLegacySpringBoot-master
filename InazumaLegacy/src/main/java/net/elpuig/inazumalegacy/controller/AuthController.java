package net.elpuig.inazumalegacy.controller;

import jakarta.servlet.http.HttpSession;
import net.elpuig.inazumalegacy.model.Usuario;
import net.elpuig.inazumalegacy.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/registro")
    public String registroForm() { return "registro"; }

    @PostMapping("/registro")
    public String registrar(@RequestParam String nombre,
                            @RequestParam String email,
                            @RequestParam String password) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(password));
        repo.save(u);
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String loginPost(@RequestParam String username,
                            @RequestParam String password,
                            HttpSession session) {
        Optional<Usuario> usuario = repo.findByNombre(username);
        if (usuario.isPresent() && passwordEncoder.matches(password, usuario.get().getPassword())) {
            session.setAttribute("usuario", usuario.get().getNombre());
            return "redirect:/inicio";
        }
        return "redirect:/login?error";
    }

    @GetMapping("/inicio")
    public String inicio(HttpSession session, Model model) {
        String nombreUsuario = (String) session.getAttribute("usuario");
        if (nombreUsuario == null) return "redirect:/login";

        repo.findByNombre(nombreUsuario).ifPresent(u -> model.addAttribute("user", u));

        return "inicio";
    }

    @GetMapping("/perfil")
    public String perfil(HttpSession session, Model model) {
        String nombreUsuario = (String) session.getAttribute("usuario");
        if (nombreUsuario == null) return "redirect:/login";

        repo.findByNombre(nombreUsuario).ifPresent(u -> model.addAttribute("user", u));
        return "perfil";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@RequestParam String nombre,
                                   @RequestParam String descripcion,
                                   @RequestParam String afiliacion,
                                   @RequestParam String rango,
                                   @RequestParam(required = false) MultipartFile foto,
                                   HttpSession session) {

        String nombreActual = (String) session.getAttribute("usuario");
        if (nombreActual == null) return "redirect:/login";

        Optional<Usuario> usuarioOpt = repo.findByNombre(nombreActual);

        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            u.setNombre(nombre);
            u.setDescripcion(descripcion);
            u.setAfiliacion(afiliacion);
            u.setRango(rango);

            repo.save(u);

            session.setAttribute("usuario", nombre);
        }

        return "redirect:/perfil?success";
    }

    @GetMapping("/")
    public String root() { return "redirect:/login"; }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}