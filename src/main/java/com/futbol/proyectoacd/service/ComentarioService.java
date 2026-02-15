package com.futbol.proyectoacd.service;

import com.futbol.proyectoacd.dto.ComentarioDTO;
import com.futbol.proyectoacd.dto.CrearComentarioRequest;
import com.futbol.proyectoacd.model.Alineacion;
import com.futbol.proyectoacd.model.Comentario;
import com.futbol.proyectoacd.model.User;
import com.futbol.proyectoacd.repository.AlineacionRepository;
import com.futbol.proyectoacd.repository.ComentarioRepository;
import com.futbol.proyectoacd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final AlineacionRepository alineacionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ComentarioDTO> obtenerComentariosPorAlineacion(Integer alineacionId) {
        Alineacion alineacion = alineacionRepository.findById(alineacionId)
                .orElseThrow(() -> new RuntimeException("Alineación no encontrada"));

        List<Comentario> comentarios = comentarioRepository.findByAlineacionOrderByCreatedAtDesc(alineacion);

        // Construir árbol de comentarios (raíz + respuestas)
        return construirArbolComentarios(comentarios);
    }

    private List<ComentarioDTO> construirArbolComentarios(List<Comentario> comentarios) {
        // Separar comentarios raíz de respuestas
        List<ComentarioDTO> resultado = new ArrayList<>();

        for (Comentario comentario : comentarios) {
            if (comentario.getRespondeA() == null) {
                // Es un comentario raíz
                resultado.add(convertirADTO(comentario));
            }
        }

        return resultado;
    }

    private ComentarioDTO convertirADTO(Comentario comentario) {
        ComentarioDTO dto = new ComentarioDTO();
        dto.setId(comentario.getId());
        dto.setMensaje(comentario.getMensaje());
        dto.setCreatedAt(comentario.getCreatedAt());

        if (comentario.getAlineacion() != null) {
            dto.setAlineacionId(comentario.getAlineacion().getId());
        }

        if (comentario.getEquipo() != null) {
            dto.setEquipoId(comentario.getEquipo().getId());
            dto.setEquipoNombre(comentario.getEquipo().getNombre());
        }

        if (comentario.getUser() != null) {
            dto.setUserId(comentario.getUser().getId());
            dto.setUserEmail(comentario.getUser().getEmail());
        }

        if (comentario.getRespondeA() != null) {
            dto.setRespondeAId(comentario.getRespondeA().getId());
        }

        return dto;
    }

    @Transactional
    public ComentarioDTO crearComentario(CrearComentarioRequest request, Integer userId) {
        // Validar que existe la alineación
        Alineacion alineacion = alineacionRepository.findById(request.getAlineacionId())
                .orElseThrow(() -> new RuntimeException("Alineación no encontrada"));

        // Obtener el usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Comentario comentario = new Comentario();
        comentario.setAlineacion(alineacion);
        comentario.setUser(user);
        comentario.setMensaje(request.getMensaje());

        // Si es una respuesta, vincular al comentario padre
        if (request.getRespondeAId() != null) {
            Comentario comentarioPadre = comentarioRepository.findById(request.getRespondeAId())
                    .orElseThrow(() -> new RuntimeException("Comentario padre no encontrado"));
            comentario.setRespondeA(comentarioPadre);
        }

        Comentario guardado = comentarioRepository.save(comentario);
        return convertirADTO(guardado);
    }

    @Transactional(readOnly = true)
    public List<ComentarioDTO> obtenerRespuestas(Integer comentarioId) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        List<Comentario> respuestas = comentarioRepository.findByRespondeA(comentario);

        return respuestas.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
}
