package net.elpuig.inazumalegacy.controller;

import jakarta.servlet.http.HttpSession;
import net.elpuig.inazumalegacy.model.Usuario;
import net.elpuig.inazumalegacy.repository.UsuarioRepository;
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
    private PasswordEncoder passwordEncoder;

    // 1. PRIMERA PÁGINA: Al abrir la app (localhost:8080) sale el HTML de invitado
    @GetMapping("/")
    public String root() {
        return "inicio_invitado";
    }

    // 2. FORMULARIOS DE ACCESO
    @GetMapping("/registro")
    public String registroForm() {
        return "registro";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // 3. LÓGICA DE REGISTRO
    @PostMapping("/registro")
    public String registrar(@RequestParam String nombre,
                            @RequestParam String email,
                            @RequestParam String password) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(password));
        repo.save(u);
        // Tras registrarse, lo mandamos al login para que entre oficialmente
        return "redirect:/login";
    }

    // 4. LÓGICA DE LOGIN
    @PostMapping("/login")
    public String loginPost(@RequestParam String username,
                            @RequestParam String password,
                            HttpSession session) {
        Optional<Usuario> usuario = repo.findByNombre(username);
        if (usuario.isPresent() && passwordEncoder.matches(password, usuario.get().getPassword())) {
            session.setAttribute("usuario", usuario.get().getNombre());
            // Si el login es correcto, entra al inicio real (el del video)
            return "redirect:/inicio";
        }
        return "redirect:/login?error";
    }

    // 5. PÁGINA DE INICIO (USUARIOS LOGUEADOS)
    @GetMapping("/inicio")
    public String inicio(HttpSession session, Model model) {
        String nombreUsuario = (String) session.getAttribute("usuario");
        // Si no hay sesión, lo mandamos a REGISTRO como pediste
        if (nombreUsuario == null) return "redirect:/registro";

        repo.findByNombre(nombreUsuario).ifPresent(u -> model.addAttribute("user", u));
        return "inicio";
    }

    // 6. PERFIL (USUARIOS LOGUEADOS)
    @GetMapping("/perfil")
    public String perfil(HttpSession session, Model model) {
        String nombreUsuario = (String) session.getAttribute("usuario");
        // Si intenta entrar sin sesión, al registro
        if (nombreUsuario == null) return "redirect:/registro";

        repo.findByNombre(nombreUsuario).ifPresent(u -> model.addAttribute("user", u));
        return "perfil";
    }

    // 7. SALIR
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        // Al salir, volvemos a la pantalla de invitado
        return "redirect:/";
    }
}