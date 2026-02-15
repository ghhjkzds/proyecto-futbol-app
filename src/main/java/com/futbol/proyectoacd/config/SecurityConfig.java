package com.futbol.proyectoacd.config;

import com.futbol.proyectoacd.model.Rol;
import com.futbol.proyectoacd.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/login.html",
                                "/register.html",
                                "/index.html",
                                "/crear-partido.html",
                                "/crear-alineacion.html",
                                "/mis-alineaciones.html",
                                "/ver-alineaciones.html",
                                "/ranking.html",
                                "/api/auth/**",
                                "/api/comentarios/alineacion/**",
                                "/api/comentarios/*/respuestas",
                                "/error",
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // Rutas que requieren autenticación (USER o ADMIN)
                        .requestMatchers("/api/partidos/equipos-laliga", "/api/equipos/**", "/api/partidos", "/api/alineaciones/**", "/api/comentarios").authenticated()
                        // Rutas solo para administrador
                        .requestMatchers("/admin/**", "/api/partidos/crear", "/api/partidos/{id}").hasRole(Rol.ADMIN.name())
                        // Rutas de usuario
                        .requestMatchers("/user/**").hasRole(Rol.USER.name())
                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )
                // Deshabilitar form login y usar JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Agregar filtro JWT antes del filtro de autenticación
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
