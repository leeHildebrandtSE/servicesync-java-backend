package com.wpc.servicesync_backend.config;

import com.wpc.servicesync_backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/api/health/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                        .requestMatchers("/api/qr/generate/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()

                        // Employee endpoints - require authentication
                        .requestMatchers("/api/employees/**").hasAnyRole("HOSTESS", "NURSE", "SUPERVISOR", "ADMIN")

                        // Session management - different roles
                        .requestMatchers(HttpMethod.POST, "/api/sessions").hasAnyRole("HOSTESS", "SUPERVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/sessions/**").hasAnyRole("HOSTESS", "NURSE", "SUPERVISOR", "ADMIN")
                        .requestMatchers("/api/sessions/**").hasAnyRole("HOSTESS", "NURSE", "SUPERVISOR", "ADMIN")

                        // File uploads
                        .requestMatchers(HttpMethod.POST, "/api/files/**").hasAnyRole("HOSTESS", "SUPERVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/files/**").hasAnyRole("HOSTESS", "NURSE", "SUPERVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/files/**").hasAnyRole("SUPERVISOR", "ADMIN")

                        // Reports and analytics
                        .requestMatchers("/api/reports/**").hasAnyRole("SUPERVISOR", "ADMIN")
                        .requestMatchers("/api/performance/**").hasAnyRole("SUPERVISOR", "ADMIN")
                        .requestMatchers("/api/dashboard/**").hasAnyRole("SUPERVISOR", "ADMIN")

                        // Hospital and ward management
                        .requestMatchers("/api/hospitals/**", "/api/wards/**").hasAnyRole("HOSTESS", "NURSE", "SUPERVISOR", "ADMIN")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}