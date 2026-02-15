package com.futbol.proyectoacd.service;

import com.futbol.proyectoacd.model.User;
import com.futbol.proyectoacd.model.Rol;
import com.futbol.proyectoacd.repository.UserRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Crear un nuevo usuario
    @PreAuthorize("hasRole('ADMIN')") // Solo los administradores pueden crear usuarios
    public User crearUsuario(String email, String password, Rol role) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El email ya está en uso");
        }

        User usuario = new User();
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password)); // Encriptar contraseña
        usuario.setRole(role);

        return userRepository.save(usuario);
    }

    // Listar todos los usuarios
    @PreAuthorize("hasRole('ADMIN')") // Solo los administradores pueden listar usuarios
    public List<User> listarUsuarios() {
        return userRepository.findAll();
    }

    // Buscar un usuario por su email
    @PreAuthorize("hasRole('ADMIN')") // Solo los administradores pueden buscar usuarios
    public Optional<User> buscarPorEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @PreAuthorize("hasRole('ADMIN')") // Solo los administradores pueden eliminar usuarios
    public void eliminarUsuario(Integer id) {
        userRepository.deleteById(id);
    }

    public User obtenerUsuarioPorId(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Transactional
    public void actualizarUsuario(Integer id, User usuarioActualizado) {
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setEmail(usuarioActualizado.getEmail());
        usuario.setRole(usuarioActualizado.getRole());

        userRepository.save(usuario);
    }

}
