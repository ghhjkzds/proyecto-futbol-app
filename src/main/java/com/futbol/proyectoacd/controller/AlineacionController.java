package com.futbol.proyectoacd.controller;

import com.futbol.proyectoacd.dto.AlineacionDTO;
import com.futbol.proyectoacd.model.Alineacion;
import com.futbol.proyectoacd.model.Equipo;
import com.futbol.proyectoacd.model.Partido;
import com.futbol.proyectoacd.model.User;
import com.futbol.proyectoacd.repository.AlineacionRepository;
import com.futbol.proyectoacd.repository.EquipoRepository;
import com.futbol.proyectoacd.repository.PartidoRepository;
import com.futbol.proyectoacd.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestión de alineaciones
 */
@RestController
@RequestMapping("/api/alineaciones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Alineaciones", description = "Gestión de alineaciones de usuarios")
public class AlineacionController {

    private final AlineacionRepository alineacionRepository;
    private final UserRepository userRepository;
    private final PartidoRepository partidoRepository;
    private final EquipoRepository equipoRepository;

    @Operation(summary = "Obtener todas las alineaciones del usuario autenticado")
    @GetMapping("/mis-alineaciones")
    public ResponseEntity<List<AlineacionDTO>> getMisAlineaciones(Authentication authentication) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            List<Alineacion> alineaciones = alineacionRepository.findByCreatedByOrderByCreatedAtDesc(user);

            List<AlineacionDTO> dtos = alineaciones.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error al obtener alineaciones del usuario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Verificar si el usuario ya tiene una alineación para un equipo en un partido")
    @GetMapping("/verificar-existente")
    public ResponseEntity<?> verificarAlineacionExistente(
            @RequestParam Integer partidoId,
            @RequestParam Integer equipoId,
            Authentication authentication
    ) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Partido partido = partidoRepository.findById(partidoId)
                    .orElseThrow(() -> new RuntimeException("Partido no encontrado"));

            Equipo equipo = equipoRepository.findById(equipoId)
                    .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

            Optional<Alineacion> alineacionExistente = alineacionRepository
                    .findByCreatedByAndPartidoAndEquipo(user, partido, equipo);

            Map<String, Object> response = new HashMap<>();
            response.put("existe", alineacionExistente.isPresent());

            if (alineacionExistente.isPresent()) {
                response.put("alineacion", convertToDTO(alineacionExistente.get()));
                response.put("message", "Ya tienes una alineación creada para este equipo en este partido");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al verificar alineación existente", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "Crear una nueva alineación desde partido de API-Football")
    @PostMapping("/from-api-football")
    public ResponseEntity<?> crearAlineacionDesdeAPI(
            @RequestBody CreateAlineacionFromAPIRequest request,
            Authentication authentication
    ) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            log.info("Creando alineación desde API-Football: Fixture {}, Team {}",
                    request.getApiFixtureId(), request.getApiTeamId());

            // Buscar o crear equipo
            Equipo equipo = equipoRepository.findByNombre(request.getTeamName())
                    .orElseGet(() -> {
                        Equipo nuevoEquipo = new Equipo();
                        nuevoEquipo.setNombre(request.getTeamName());
                        nuevoEquipo.setUser(user);
                        return equipoRepository.save(nuevoEquipo);
                    });

            // Buscar o crear partido
            // Buscamos por fecha y equipos para evitar duplicados
            Equipo equipoLocal = equipoRepository.findByNombre(request.getHomeTeamName())
                    .orElseGet(() -> {
                        Equipo nuevoEquipo = new Equipo();
                        nuevoEquipo.setNombre(request.getHomeTeamName());
                        nuevoEquipo.setUser(user);
                        return equipoRepository.save(nuevoEquipo);
                    });

            Equipo equipoVisitante = equipoRepository.findByNombre(request.getAwayTeamName())
                    .orElseGet(() -> {
                        Equipo nuevoEquipo = new Equipo();
                        nuevoEquipo.setNombre(request.getAwayTeamName());
                        nuevoEquipo.setUser(user);
                        return equipoRepository.save(nuevoEquipo);
                    });

            // Buscar partido existente o crear uno nuevo
            Partido partido = partidoRepository.findByEquipoLocalAndEquipoVisitanteAndFecha(
                    equipoLocal, equipoVisitante, request.getMatchDate())
                    .orElseGet(() -> {
                        Partido nuevoPartido = new Partido();
                        nuevoPartido.setEquipoLocal(equipoLocal);
                        nuevoPartido.setEquipoVisitante(equipoVisitante);
                        nuevoPartido.setFecha(request.getMatchDate());
                        nuevoPartido.setCreadoPor(user);
                        return partidoRepository.save(nuevoPartido);
                    });

            // VALIDACIÓN: Verificar que el partido no se haya jugado todavía
            LocalDateTime ahora = LocalDateTime.now();
            if (partido.getFecha().isBefore(ahora)) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "El partido ya se ha jugado");
                error.put("message", "No puedes crear alineaciones para partidos que ya se han jugado");
                error.put("fechaPartido", partido.getFecha());
                error.put("fechaActual", ahora);
                error.put("partidoNombre", partido.getEquipoLocal().getNombre() + " vs " + partido.getEquipoVisitante().getNombre());

                log.warn("Intento de crear alineación para partido ya jugado desde API. Fixture ID: {}, Usuario: {}",
                        request.getApiFixtureId(), user.getEmail());

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Validar que el usuario no tenga ya una alineación para este equipo en este partido
            boolean existeAlineacion = alineacionRepository.existsByCreatedByAndPartidoAndEquipo(user, partido, equipo);

            if (existeAlineacion) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Ya tienes una alineación creada para este equipo en este partido");
                error.put("message", "Solo puedes crear una alineación por equipo por partido. Si deseas modificarla, elimina la anterior primero.");
                error.put("partidoNombre", partido.getEquipoLocal().getNombre() + " vs " + partido.getEquipoVisitante().getNombre());
                error.put("equipoNombre", equipo.getNombre());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }

            // Crear alineación
            Alineacion alineacion = new Alineacion();
            alineacion.setPartido(partido);
            alineacion.setEquipo(equipo);
            alineacion.setAlineacion(request.getAlineacion());
            alineacion.setCreatedBy(user);

            alineacion = alineacionRepository.save(alineacion);

            log.info("Alineación creada desde API-Football: Usuario {}, Fixture {}, Team {}",
                    user.getEmail(), request.getApiFixtureId(), request.getApiTeamId());

            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(alineacion));
        } catch (Exception e) {
            log.error("Error al crear alineación desde API-Football", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "Crear una nueva alineación")
    @PostMapping
    public ResponseEntity<?> crearAlineacion(
            @RequestBody CreateAlineacionRequest request,
            Authentication authentication
    ) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Partido partido = partidoRepository.findById(request.getPartidoId())
                    .orElseThrow(() -> new RuntimeException("Partido no encontrado"));

            // VALIDACIÓN: Verificar que el partido no se haya jugado todavía
            LocalDateTime ahora = LocalDateTime.now();
            if (partido.getFecha().isBefore(ahora)) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "El partido ya se ha jugado");
                error.put("message", "No puedes crear alineaciones para partidos que ya se han jugado");
                error.put("fechaPartido", partido.getFecha());
                error.put("fechaActual", ahora);
                error.put("partidoNombre", partido.getEquipoLocal().getNombre() + " vs " + partido.getEquipoVisitante().getNombre());

                log.warn("Intento de crear alineación para partido ya jugado. Partido ID: {}, Fecha: {}, Usuario: {}",
                        partido.getId(), partido.getFecha(), user.getEmail());

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            Equipo equipo = equipoRepository.findById(request.getEquipoId())
                    .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

            // Validar que el usuario no tenga ya una alineación para este equipo en este partido
            boolean existeAlineacion = alineacionRepository.existsByCreatedByAndPartidoAndEquipo(user, partido, equipo);

            if (existeAlineacion) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Ya tienes una alineación creada para este equipo en este partido");
                error.put("message", "Solo puedes crear una alineación por equipo por partido. Si deseas modificarla, elimina la anterior primero.");
                error.put("partidoNombre", partido.getEquipoLocal().getNombre() + " vs " + partido.getEquipoVisitante().getNombre());
                error.put("equipoNombre", equipo.getNombre());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }

            // Crear alineación
            Alineacion alineacion = new Alineacion();
            alineacion.setPartido(partido);
            alineacion.setEquipo(equipo);
            alineacion.setAlineacion(request.getAlineacion());
            alineacion.setCreatedBy(user);

            alineacion = alineacionRepository.save(alineacion);

            log.info("Alineación creada: Usuario {}, Partido {}, Equipo {}",
                    user.getEmail(), partido.getId(), equipo.getNombre());

            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(alineacion));
        } catch (Exception e) {
            log.error("Error al crear alineación", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "Eliminar una alineación")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarAlineacion(
            @PathVariable Integer id,
            Authentication authentication
    ) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Alineacion alineacion = alineacionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Alineación no encontrada"));

            // Verificar que el usuario sea el creador
            if (!alineacion.getCreatedBy().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "No tienes permiso para eliminar esta alineación"));
            }

            alineacionRepository.delete(alineacion);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Alineación eliminada exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al eliminar alineación", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "Obtener todas las alineaciones de un partido")
    @GetMapping("/partido/{partidoId}")
    public ResponseEntity<?> getAlineacionesPorPartido(@PathVariable Integer partidoId) {
        try {
            Partido partido = partidoRepository.findById(partidoId)
                    .orElseThrow(() -> new RuntimeException("Partido no encontrado"));

            List<Alineacion> alineaciones = alineacionRepository.findByPartido(partido);

            List<AlineacionDTO> dtos = alineaciones.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            // Agrupar por equipo
            Map<String, List<AlineacionDTO>> alineacionesPorEquipo = dtos.stream()
                    .collect(Collectors.groupingBy(AlineacionDTO::getEquipoNombre));

            Map<String, Object> response = new HashMap<>();
            response.put("partido", Map.of(
                    "id", partido.getId(),
                    "nombre", partido.getEquipoLocal().getNombre() + " vs " + partido.getEquipoVisitante().getNombre(),
                    "fecha", partido.getFecha(),
                    "equipoLocal", partido.getEquipoLocal().getNombre(),
                    "equipoVisitante", partido.getEquipoVisitante().getNombre()
            ));
            response.put("alineaciones", alineacionesPorEquipo);
            response.put("totalAlineaciones", dtos.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al obtener alineaciones del partido", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Convierte Alineacion a AlineacionDTO
     */
    private AlineacionDTO convertToDTO(Alineacion alineacion) {
        Partido partido = alineacion.getPartido();
        String partidoNombre = partido.getEquipoLocal().getNombre() + " vs " +
                               partido.getEquipoVisitante().getNombre();

        Equipo equipo = alineacion.getEquipo();

        return new AlineacionDTO(
                alineacion.getId(),
                partido.getId(),
                partidoNombre,
                partido.getFecha(),
                equipo.getId(),
                equipo.getNombre(),
                alineacion.getAlineacion(),
                alineacion.getCreatedAt(),
                alineacion.getCreatedBy().getEmail()
        );
    }

    /**
     * Request DTO para crear alineación
     */
    @lombok.Data
    public static class CreateAlineacionRequest {
        private Integer partidoId;
        private Integer equipoId;
        private com.futbol.proyectoacd.model.EquipoDetalles alineacion;
    }

    /**
     * Request DTO para crear alineación desde API-Football
     */
    @lombok.Data
    public static class CreateAlineacionFromAPIRequest {
        private Integer apiFixtureId;
        private Integer apiTeamId;
        private String teamName;
        private String homeTeamName;
        private String awayTeamName;
        private LocalDateTime matchDate;
        private com.futbol.proyectoacd.model.EquipoDetalles alineacion;
    }
}
