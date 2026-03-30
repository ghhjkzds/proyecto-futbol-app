package com.futbol.proyectoacd.controller;

import com.futbol.proyectoacd.dto.EquipoDTO;
import com.futbol.proyectoacd.model.Equipo;
import com.futbol.proyectoacd.model.EquipoDetalles;
import com.futbol.proyectoacd.model.User;
import com.futbol.proyectoacd.model.apifootball.PlayerData;
import com.futbol.proyectoacd.model.apifootball.TeamData;
import com.futbol.proyectoacd.repository.UserRepository;
import com.futbol.proyectoacd.service.EquipoIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de equipos con integración de API-Football
 */
@RestController
@RequestMapping("/api/equipos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Equipos", description = "Gestión de equipos con integración de API-Football")
public class EquipoController {

    private final EquipoIntegrationService equipoIntegrationService;
    private final UserRepository userRepository;

    @Operation(summary = "Buscar equipos en API-Football")
    @GetMapping("/api-football/search")
    public ResponseEntity<List<TeamData>> searchTeams(@RequestParam String name) {
        try {
            List<TeamData> teams = equipoIntegrationService.searchTeamsInApi(name);
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            log.error("Error al buscar equipos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Obtener jugadores disponibles de un equipo")
    @GetMapping("/api-football/team/{teamId}/players")
    public ResponseEntity<List<PlayerData>> getTeamPlayers(
            @PathVariable Integer teamId,
            @RequestParam(defaultValue = "2024") Integer season
    ) {
        try {
            List<PlayerData> players = equipoIntegrationService.getAvailablePlayers(teamId, season);
            return ResponseEntity.ok(players);
        } catch (Exception e) {
            log.error("Error al obtener jugadores", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Obtener plantilla completa (squad) de un equipo")
    @GetMapping("/api-football/{teamId}/squad/{season}")
    public ResponseEntity<List<PlayerData>> getTeamSquad(
            @PathVariable Integer teamId,
            @PathVariable Integer season
    ) {
        try {
            log.info("Obteniendo squad del equipo {} temporada {}", teamId, season);
            List<PlayerData> players = equipoIntegrationService.getAvailablePlayers(teamId, season);
            return ResponseEntity.ok(players);
        } catch (Exception e) {
            log.error("Error al obtener squad del equipo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Obtener alineaciones de un partido")
    @GetMapping("/api-football/fixture/{fixtureId}/lineups")
    public ResponseEntity<List<EquipoDetalles>> getFixtureLineups(@PathVariable Integer fixtureId) {
        try {
            List<EquipoDetalles> lineups = equipoIntegrationService.getFixtureLineups(fixtureId);
            return ResponseEntity.ok(lineups);
        } catch (Exception e) {
            log.error("Error al obtener alineaciones del partido", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Crear equipo desde API-Football con alineación automática")
    @PostMapping("/create-from-api")
    public ResponseEntity<?> createEquipoFromApi(
            @RequestParam Integer apiTeamId,
            @RequestParam(defaultValue = "2024") Integer season,
            Authentication authentication
    ) {
        try {
            // Obtener usuario autenticado
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Equipo equipo = equipoIntegrationService.createEquipoFromApiFootball(apiTeamId, user, season);

            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(equipo));
        } catch (Exception e) {
            log.error("Error al crear equipo desde API", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "Crear equipo personalizado seleccionando jugadores")
    @PostMapping("/create-custom")
    public ResponseEntity<?> createCustomEquipo(
            @RequestBody CreateCustomEquipoRequest request,
            Authentication authentication
    ) {
        try {
            // Obtener usuario autenticado
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Equipo equipo = equipoIntegrationService.createCustomEquipo(
                    request.getApiTeamId(),
                    user,
                    request.getSeason(),
                    request.getFormation(),
                    request.getTitularesIds(),
                    request.getSuplentesIds()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(equipo));
        } catch (Exception e) {
            log.error("Error al crear equipo personalizado", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "Actualizar alineación de un equipo desde un partido")
    @PutMapping("/{equipoId}/update-lineup")
    public ResponseEntity<?> updateLineupFromFixture(
            @PathVariable Integer equipoId,
            @RequestParam Integer fixtureId,
            Authentication authentication
    ) {
        try {
            Equipo equipo = equipoIntegrationService.updateAlineacionFromFixture(equipoId, fixtureId);
            return ResponseEntity.ok(convertToDTO(equipo));
        } catch (Exception e) {
            log.error("Error al actualizar alineación", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Convierte Equipo a EquipoDTO
     */
    private EquipoDTO convertToDTO(Equipo equipo) {
        return new EquipoDTO(
                equipo.getId(),
                equipo.getUser().getId(),
                equipo.getUser().getEmail(),
                equipo.getNombre(),
                equipo.getAlineacion(),
                equipo.getCreatedAt()
        );
    }

    /**
     * Request DTO para crear equipo personalizado
     */
    @lombok.Data
    public static class CreateCustomEquipoRequest {
        private Integer apiTeamId;
        private Integer season = 2024;
        private String formation = "4-3-3";
        private List<Integer> titularesIds;
        private List<Integer> suplentesIds;
    }
}
