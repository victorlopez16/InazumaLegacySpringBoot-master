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
                // 1. Desactivamos CSRF porque los WebSockets y las APIs de chat suelen chocar con esto
                .csrf(csrf -> csrf.disable())

                // 2. IMPORTANTE: Permitir X-Frame-Options
                // SockJS (el que usas en el HTML) a veces usa iframes ocultos.
                // Si no deshabilitamos esto, el navegador bloquea la conexión.
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                )

                .authorizeHttpRequests(auth -> auth
                        // 3. Permitir estáticos y rutas de acceso
                        .requestMatchers("/", "/registro", "/login", "/css/**", "/js/**", "/video/**", "/img/**").permitAll()

                        // 4. LIBERAR EL ENDPOINT DEL WEBSOCKET
                        // Debe coincidir con registry.addEndpoint("/ws-inazuma") de tu WebSocketConfig
                        .requestMatchers("/ws-inazuma/**").permitAll()

                        // 5. El resto lo dejamos libre por ahora para que no te bloquee nada más
                        .anyRequest().permitAll()
                )

                // 6. Deshabilitamos logins por defecto para usar tu lógica personalizada
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}