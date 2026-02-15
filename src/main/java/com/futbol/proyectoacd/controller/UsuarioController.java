package com.futbol.proyectoacd.controller;

import com.futbol.proyectoacd.model.Rol;
import com.futbol.proyectoacd.model.User;
import com.futbol.proyectoacd.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UserService userService;

    public UsuarioController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')") // Solo los administradores pueden listar usuarios
    @GetMapping("/lista")
    public String listarUsuarios(Model model) {
        List<User> usuarios = userService.listarUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "usuarios/lista"; // Apunta a una plantilla Thymeleaf
    }

    @PreAuthorize("hasRole('ADMIN')") // Solo los administradores pueden crear nuevos usuarios
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoUsuario(Model model) {
        model.addAttribute("usuario", new User());
        model.addAttribute("roles", Rol.values());
        return "usuarios/nuevo";
    }

    @PreAuthorize("hasRole('ADMIN')") // Solo los administradores pueden guardar nuevos usuarios
    @PostMapping
    public String crearUsuario(@RequestParam String email,
                               @RequestParam String password,
                               @RequestParam Rol role) {
        userService.crearUsuario(email, password, role);
        return "redirect:/usuarios/lista";
    }

    @PreAuthorize("hasRole('ADMIN')") // Solo los administradores pueden eliminar usuarios
    @PostMapping("/{id}/eliminar")
    public String eliminarUsuario(@PathVariable Integer id) {
        userService.eliminarUsuario(id);
        return "redirect:/usuarios/lista";
    }

    @GetMapping("/{id}/editar")
    public String editarUsuario(@PathVariable Integer id, Model model) {
        User usuario = userService.obtenerUsuarioPorId(id);
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", Rol.values()); // Cargar roles disponibles
        return "usuarios/editar_usuario";
    }

    @PostMapping("/{id}/editar")
    public String actualizarUsuario(@PathVariable Integer id, @ModelAttribute User usuario) {
        userService.actualizarUsuario(id, usuario);
        return "redirect:/usuarios/lista";
    }

}
