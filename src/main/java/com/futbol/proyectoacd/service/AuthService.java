package com.futbol.proyectoacd.service;

import com.futbol.proyectoacd.dto.AuthResponse;
import com.futbol.proyectoacd.dto.LoginRequest;
import com.futbol.proyectoacd.dto.RegisterRequest;
import com.futbol.proyectoacd.model.Rol;
import com.futbol.proyectoacd.model.User;
import com.futbol.proyectoacd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        log.info("Intentando registrar usuario: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email ya registrado: {}", request.getEmail());
            throw new RuntimeException("Email ya registrado");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Rol.USER); // Asegurar que se asigna el rol USER por defecto

        user = userRepository.save(user);
        log.info("Usuario registrado exitosamente: {} con ID: {} y rol: {}", user.getEmail(), user.getId(), user.getRole());

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login 7y7 request: {}", request.getPassword());
        log.info("Intento 787de login para: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado: {}", request.getEmail());
                    return new RuntimeException("Credenciales inválidas");
                });

       // log.warn("Usuario encontrado: {}, verificando contraseña...", user.getEmail());

        log.warn("hola");
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Contraseña incorrecta para: {}", request.getEmail());
            throw new RuntimeException("Credenciales inválidas");
        }

        log.info("Login exitoso para: {} con rol: {}", user.getEmail(), user.getRole());
        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }
}
