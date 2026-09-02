package com.lojaagro.estoque_api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(org.springframework.security.config.Customizer.withDefaults()) // LEMBRANDO AQUI PRA ALTERAR DEEPOIS SE FOR PREECISO( QUEEM VEER ISSO, SAIBA Q TIREI A TELA DEE LOGIN INICIAL SO PRA FACILITAR A PRODUÇÃO)
        .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll() // Libera TUDO sem precisar de login(TEMPORARIAMENTE, LEMBRAR!!)
        );
    
    return http.build();
}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}