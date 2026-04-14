package net.elpuig.inazumalegacy.controller;

import net.elpuig.inazumalegacy.model.Usuario;
import net.elpuig.inazumalegacy.repository.UsuarioRepository;
import net.elpuig.inazumalegacy.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Controller
public class PasswordResetController {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @GetMapping("/recuperar-password")
    public String recuperarForm() {
        return "recuperar-paso1";
    }

    @PostMapping("/recuperar-password")
    public String recuperarPost(@RequestParam String email,
                                RedirectAttributes ra) {
        Optional<Usuario> opt = repo.findByEmail(email);

        if (opt.isPresent()) {
            Usuario u = opt.get();

            String codigo = String.format("%06d", new Random().nextInt(999999));

            u.setResetToken(codigo);
            u.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
            repo.save(u);

            emailService.enviarCodigoRecuperacion(email, codigo);
        }

        ra.addFlashAttribute("email", email);
        return "redirect:/verificar-codigo";
    }

    @GetMapping("/verificar-codigo")
    public String verificarForm(Model model) {
        return "recuperar-paso2";
    }

    @PostMapping("/verificar-codigo")
    public String verificarPost(@RequestParam String email,
                                @RequestParam String codigo,
                                RedirectAttributes ra) {
        Optional<Usuario> opt = repo.findByEmail(email);

        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Email no encontrado.");
            ra.addFlashAttribute("email", email);
            return "redirect:/verificar-codigo";
        }

        Usuario u = opt.get();

        if (u.getResetTokenExpiry() == null ||
                LocalDateTime.now().isAfter(u.getResetTokenExpiry())) {
            ra.addFlashAttribute("error", "El código ha expirado. Solicita uno nuevo.");
            ra.addFlashAttribute("email", email);
            return "redirect:/verificar-codigo";
        }

        if (!codigo.equals(u.getResetToken())) {
            ra.addFlashAttribute("error", "Código incorrecto. Inténtalo de nuevo.");
            ra.addFlashAttribute("email", email);
            return "redirect:/verificar-codigo";
        }

        ra.addFlashAttribute("email", email);
        ra.addFlashAttribute("token", codigo);
        return "redirect:/nueva-password";
    }

    @GetMapping("/nueva-password")
    public String nuevaPasswordForm() {
        return "recuperar-paso3";
    }

    @PostMapping("/nueva-password")
    public String nuevaPasswordPost(@RequestParam String email,
                                    @RequestParam String token,
                                    @RequestParam String password,
                                    @RequestParam String password2,
                                    RedirectAttributes ra) {
        if (!password.equals(password2)) {
            ra.addFlashAttribute("error", "Las contraseñas no coinciden.");
            ra.addFlashAttribute("email", email);
            ra.addFlashAttribute("token", token);
            return "redirect:/nueva-password";
        }

        if (password.length() < 6) {
            ra.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            ra.addFlashAttribute("email", email);
            ra.addFlashAttribute("token", token);
            return "redirect:/nueva-password";
        }

        Optional<Usuario> opt = repo.findByEmail(email);
        if (opt.isEmpty()) {
            return "redirect:/login";
        }

        Usuario u = opt.get();

        if (!token.equals(u.getResetToken()) ||
                LocalDateTime.now().isAfter(u.getResetTokenExpiry())) {
            ra.addFlashAttribute("error", "Sesión expirada. Inicia el proceso de nuevo.");
            return "redirect:/recuperar-password";
        }

        u.setPassword(passwordEncoder.encode(password));
        u.setResetToken(null);
        u.setResetTokenExpiry(null);
        repo.save(u);

        ra.addFlashAttribute("exito", "¡Contraseña cambiada! Ya puedes iniciar sesión.");
        return "redirect:/login";
    }
}