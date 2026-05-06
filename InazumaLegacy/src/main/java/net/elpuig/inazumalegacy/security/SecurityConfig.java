package net.elpuig.inazumalegacy.security;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desactivamos CSRF para evitar bloqueos en formularios
                .authorizeHttpRequests(auth -> auth
                        // ESTO ES LO IMPORTANTE: Permitir la raíz y las páginas de acceso
                        .requestMatchers("/", "/registro", "/login", "/css/**", "/js/**").permitAll()
                        .anyRequest().permitAll()
                )
                // Deshabilitamos el formulario automático de Spring que te está dando por saco
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}