package com.finc.platform.config;

import com.finc.platform.security.RoleGuardFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private RoleGuardFilter roleGuardFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .addFilterBefore(roleGuardFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/api/auth/**",
                        "/api/presence/**",
                        "/api/search/**",
                        "/api/users/**",
                        "/api/departments/**",
                        "/api/tasks/**",
                        "/api/files/**",
                        "/api/folders/**",
                        "/api/channels/**",
                        "/api/kb/**",
                        "/api/messages/**",
                        "/api/notifications/**",
                        "/api/calendar/**",
                        "/api/admin/**",
                        "/index.html", "/css/**", "/js/**", "/images/**", "/", "/**/*.html"
                ).permitAll()
                .anyRequest().permitAll()
            );
        return http.build();
    }
}