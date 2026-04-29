package net.elpuig.inazumalegacy.service;

import net.elpuig.inazumalegacy.model.Usuario;
import net.elpuig.inazumalegacy.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String nombreOEmail) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByNombreOrEmail(nombreOEmail, nombreOEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con: " + nombreOEmail));

        return new User(
                usuario.getNombre(),
                usuario.getPassword(),
                Collections.emptyList()
        );
    }
}