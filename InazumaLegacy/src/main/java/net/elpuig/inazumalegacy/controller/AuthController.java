package net.elpuig.inazumalegacy.controller;

import jakarta.servlet.http.HttpSession;
import net.elpuig.inazumalegacy.model.Usuario;
import net.elpuig.inazumalegacy.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() { return "ogin"; }

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
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
