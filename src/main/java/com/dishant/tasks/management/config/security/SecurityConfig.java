package com.dishant.tasks.management.config.security;

import com.dishant.tasks.management.security.JwtFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@Slf4j
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtFilter jwtFilter, UserDetailsService userDetailsService) {
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Initializing SecurityFilterChain...");

        return http
                .cors(cors -> log.debug("CORS enabled")) // default
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                            "/actuator/**", "/v1/api/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
                    ).permitAll();
                    auth.requestMatchers("/v1/api/admin/**").hasRole("ADMIN");
                    auth.anyRequest().authenticated();
                    log.debug("Authorization rules applied: Auth endpoints are publicly accessible");
                })
                .sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                    log.debug("Session creation policy set to STATELESS");
                })
                .userDetailsService(userDetailsService)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("PasswordEncoder (BCrypt) bean initialized");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        try {
            AuthenticationManager manager = config.getAuthenticationManager();
            log.info("AuthenticationManager bean initialized");
            return manager;
        } catch (Exception e) {
            log.error("Failed to initialize AuthenticationManager: {}", e.getMessage(), e);
            throw e;
        }
    }
}
