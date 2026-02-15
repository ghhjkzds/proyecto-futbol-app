package com.futbol.proyectoacd.controller;

import com.futbol.proyectoacd.dto.PartidoDTO;
import com.futbol.proyectoacd.model.Equipo;
import com.futbol.proyectoacd.model.Partido;
import com.futbol.proyectoacd.model.User;
import com.futbol.proyectoacd.model.apifootball.TeamData;
import com.futbol.proyectoacd.repository.EquipoRepository;
import com.futbol.proyectoacd.repository.PartidoRepository;
import com.futbol.proyectoacd.repository.UserRepository;
import com.futbol.proyectoacd.service.ApiFootballService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestión de partidos
 */
@RestController
@RequestMapping("/api/partidos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Partidos", description = "Gestión de partidos de fútbol")
public class PartidoController {

    private final PartidoRepository partidoRepository;
    private final EquipoRepository equipoRepository;
    private final UserRepository userRepository;
    private final ApiFootballService apiFootballService;

    @Operation(summary = "Obtener equipos de La Liga desde API-Football")
    @GetMapping("/equipos-laliga")
    public ResponseEntity<?> getEquiposLaLiga() {
        try {
            // La Liga ID: 140
            List<TeamData> teams = apiFootballService.searchTeamsByLeague(140, 2024);
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            log.error("Error al obtener equipos de La Liga", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Obtener equipos de una liga específica desde API-Football")
    @GetMapping("/equipos-liga/{leagueId}")
    public ResponseEntity<?> getEquiposPorLiga(
            @PathVariable Integer leagueId,
            @RequestParam(defaultValue = "2024") Integer season
    ) {
        try {
            log.info("Obteniendo equipos de la liga {} temporada {}", leagueId, season);

            // Validar que la liga sea una de las 5 ligas permitidas
            List<Integer> ligasPermitidas = List.of(140, 39, 135, 78, 61);
            if (!ligasPermitidas.contains(leagueId)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Liga no permitida. Solo se permiten: La Liga (140), Premier League (39), Serie A (135), Bundesliga (78), Ligue 1 (61)");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            List<TeamData> teams = apiFootballService.searchTeamsByLeague(leagueId, season);
            log.info("Encontrados {} equipos de la liga {}", teams.size(), leagueId);

            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            log.error("Error al obtener equipos de la liga {}", leagueId, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Obtener partidos programados desde la base de datos")
    @GetMapping("/api-football/scheduled")
    public ResponseEntity<?> getPartidosProgramados() {
        try {
            log.info("Obteniendo partidos programados desde la base de datos");

            // Obtener todos los partidos y filtrar los futuros
            java.time.LocalDateTime ahora = java.time.LocalDateTime.now();

            List<Partido> todosPartidos = partidoRepository.findByOrderByFechaDesc();

            List<PartidoDTO> partidosFuturos = todosPartidos.stream()
                    .filter(p -> p.getFecha().isAfter(ahora))
                    .map(this::convertToDTO)
                    .sorted((p1, p2) -> p1.getFecha().compareTo(p2.getFecha())) // Ordenar por fecha ascendente
                    .collect(Collectors.toList());

            log.info("Encontrados {} partidos programados en la base de datos", partidosFuturos.size());

            return ResponseEntity.ok(partidosFuturos);

        } catch (Exception e) {
            log.error("Error al obtener partidos programados de la base de datos", e);

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Crear un nuevo partido")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/crear")
    public ResponseEntity<?> crearPartido(
            @RequestBody CreatePartidoRequest request,
            Authentication authentication
    ) {
        try {
            // Validar que los nombres no sean nulos o vacíos
            if (request.getEquipoLocalNombre() == null || request.getEquipoLocalNombre().trim().isEmpty()) {
                throw new RuntimeException("El nombre del equipo local es requerido");
            }

            if (request.getEquipoVisitanteNombre() == null || request.getEquipoVisitanteNombre().trim().isEmpty()) {
                throw new RuntimeException("El nombre del equipo visitante es requerido");
            }

            log.info("Creando partido: {} vs {}", request.getEquipoLocalNombre(), request.getEquipoVisitanteNombre());

            // Obtener usuario autenticado
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Obtener o crear equipo local
            Equipo equipoLocal = obtenerOCrearEquipo(request.getEquipoLocalId(), request.getEquipoLocalNombre(), user);

            // Obtener o crear equipo visitante
            Equipo equipoVisitante = obtenerOCrearEquipo(request.getEquipoVisitanteId(), request.getEquipoVisitanteNombre(), user);

            // Validar que no sean el mismo equipo
            if (equipoLocal.getId().equals(equipoVisitante.getId())) {
                throw new RuntimeException("No se puede crear un partido con el mismo equipo");
            }

            // Crear partido
            Partido partido = new Partido();
            partido.setEquipoLocal(equipoLocal);
            partido.setEquipoVisitante(equipoVisitante);
            partido.setFecha(request.getFecha());
            partido.setCreadoPor(user);

            partido = partidoRepository.save(partido);

            log.info("Partido creado exitosamente con ID: {}", partido.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(partido));
        } catch (Exception e) {
            log.error("Error al crear partido", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Obtiene un equipo de la BD o lo crea si no existe
     */
    private Equipo obtenerOCrearEquipo(Integer apiFootballId, String nombre, User user) {
        log.debug("Buscando equipo: {} (API-Football ID: {})", nombre, apiFootballId);

        // Buscar equipo por nombre (ya que el ID de API-Football no coincide con el ID de BD)
        Optional<Equipo> equipoExistente = equipoRepository.findByNombre(nombre);

        if (equipoExistente.isPresent()) {
            log.debug("Equipo encontrado en BD: {} (ID: {})", nombre, equipoExistente.get().getId());
            return equipoExistente.get();
        }

        // Si no existe, crear uno nuevo
        log.info("Creando nuevo equipo en BD: {}", nombre);
        Equipo nuevoEquipo = new Equipo();
        nuevoEquipo.setNombre(nombre);
        nuevoEquipo.setUser(user);
        nuevoEquipo.setVotos(0);

        Equipo equipoGuardado = equipoRepository.save(nuevoEquipo);
        log.info("Equipo creado exitosamente: {} (ID: {})", nombre, equipoGuardado.getId());

        return equipoGuardado;
    }

    @Operation(summary = "Listar todos los partidos")
    @GetMapping
    public ResponseEntity<List<PartidoDTO>> listarPartidos() {
        try {
            List<Partido> partidos = partidoRepository.findByOrderByFechaDesc();
            List<PartidoDTO> partidosDTO = partidos.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(partidosDTO);
        } catch (Exception e) {
            log.error("Error al listar partidos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Obtener partido por ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPartido(@PathVariable Integer id) {
        try {
            Partido partido = partidoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Partido no encontrado"));
            return ResponseEntity.ok(convertToDTO(partido));
        } catch (Exception e) {
            log.error("Error al obtener partido", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(summary = "Eliminar partido")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPartido(@PathVariable Integer id) {
        try {
            if (!partidoRepository.existsById(id)) {
                throw new RuntimeException("Partido no encontrado");
            }
            partidoRepository.deleteById(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Partido eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al eliminar partido", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Convierte Partido a PartidoDTO
     */
    private PartidoDTO convertToDTO(Partido partido) {
        return new PartidoDTO(
                partido.getId(),
                partido.getEquipoLocal().getId(),
                partido.getEquipoLocal().getNombre(),
                partido.getEquipoVisitante().getId(),
                partido.getEquipoVisitante().getNombre(),
                partido.getFecha(),
                partido.getCreadoPor().getId(),
                partido.getCreadoPor().getEmail()
        );
    }

    /**
     * Request DTO para crear partido
     */
    @lombok.Data
    public static class CreatePartidoRequest {
        private Integer equipoLocalId;
        private String equipoLocalNombre;
        private Integer equipoVisitanteId;
        private String equipoVisitanteNombre;
        private LocalDateTime fecha;
    }
}
