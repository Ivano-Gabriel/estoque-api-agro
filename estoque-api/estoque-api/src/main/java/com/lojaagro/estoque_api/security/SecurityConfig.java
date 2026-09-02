package com.lojaagro.estoque_api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // variável jwtFilter e o construtor removidos - lembrannnndo que no futuro tlvz tenha login

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(org.springframework.security.config.Customizer.withDefaults()) // LEMBRANDO AQUI PRA ALTERAR DEPOIS SE FOR PRECISO
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