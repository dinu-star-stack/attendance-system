package com.example.attendance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF so your custom forms work
                .csrf(csrf -> csrf.disable())

                // Authorize requests
                .authorizeHttpRequests(auth -> auth
                        // Public access to all login and password pages + static resources
                        .requestMatchers(
                                "/admin/login",
                                "/admin/logout",
                                "/student/login",
                                "/student/set-password/**",
                                "/lecturer/login",
                                "/lecturer/set-password/**",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        // Everything else allowed, we handle session check manually in controllers
                        .anyRequest().permitAll()
                )

                // Disable Spring Security default login form
                .formLogin(Customizer.withDefaults())

                // Disable Spring Security default logout handling
                .logout(Customizer.withDefaults());

        return http.build();
    }
}