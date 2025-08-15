package com.wpc.servicesync_backend.config;

import com.wpc.servicesync_backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
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
    private final Environment environment;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private String[] allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Clean security headers configuration using StaticHeadersWriter
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                                .maxAgeInSeconds(31536000)
                                .includeSubDomains(true)
                                .preload(true))
                        // Add all security headers using StaticHeadersWriter to avoid deprecated methods
                        .addHeaderWriter(new StaticHeadersWriter("X-Content-Type-Options", "nosniff"))
                        .addHeaderWriter(new StaticHeadersWriter("X-XSS-Protection", "1; mode=block"))
                        .addHeaderWriter(new StaticHeadersWriter("Referrer-Policy", "strict-origin-when-cross-origin"))
                        .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy", "camera=(), microphone=(), geolocation=()"))
                        .addHeaderWriter(new StaticHeadersWriter("X-Permitted-Cross-Domain-Policies", "none"))
                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/api/health/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
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

        // Use environment-specific origins
        if (isProductionEnvironment()) {
            configuration.setAllowedOrigins(Arrays.asList(
                    "https://servicesync.co.za",
                    "https://app.servicesync.co.za",
                    "https://admin.servicesync.co.za"
            ));
        } else {
            // Development origins - handle comma-separated string properly
            List<String> origins;
            if (allowedOrigins.length == 1 && allowedOrigins[0].contains(",")) {
                // Split comma-separated string
                origins = Arrays.asList(allowedOrigins[0].split(",\\s*"));
            } else {
                origins = Arrays.asList(allowedOrigins);
            }
            configuration.setAllowedOrigins(origins);
        }

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
        return new BCryptPasswordEncoder(12); // Stronger work factor for production
    }

    private boolean isProductionEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();
        return Arrays.asList(activeProfiles).contains("prod");
    }
}