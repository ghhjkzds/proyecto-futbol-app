package com.futbol.proyectoacd.controller;

import com.futbol.proyectoacd.dto.ComentarioDTO;
import com.futbol.proyectoacd.dto.CrearComentarioRequest;
import com.futbol.proyectoacd.service.ComentarioService;
import com.futbol.proyectoacd.service.JwtService;
import com.futbol.proyectoacd.model.User;
import com.futbol.proyectoacd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comentarios")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @GetMapping("/alineacion/{alineacionId}")
    public ResponseEntity<List<ComentarioDTO>> obtenerComentariosPorAlineacion(
            @PathVariable Integer alineacionId
    ) {
        List<ComentarioDTO> comentarios = comentarioService.obtenerComentariosPorAlineacion(alineacionId);
        return ResponseEntity.ok(comentarios);
    }

    @GetMapping("/{comentarioId}/respuestas")
    public ResponseEntity<List<ComentarioDTO>> obtenerRespuestas(
            @PathVariable Integer comentarioId
    ) {
        List<ComentarioDTO> respuestas = comentarioService.obtenerRespuestas(comentarioId);
        return ResponseEntity.ok(respuestas);
    }

    @PostMapping
    public ResponseEntity<?> crearComentario(
            @RequestBody CrearComentarioRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);

            // Obtener usuario por email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            ComentarioDTO comentario = comentarioService.crearComentario(request, user.getId());
            return ResponseEntity.ok(comentario);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", e.getMessage())
            );
        }
    }
}
