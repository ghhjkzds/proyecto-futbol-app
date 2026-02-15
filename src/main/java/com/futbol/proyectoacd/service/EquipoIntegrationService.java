package com.futbol.proyectoacd.service;

import com.futbol.proyectoacd.model.Equipo;
import com.futbol.proyectoacd.model.EquipoDetalles;
import com.futbol.proyectoacd.model.User;
import com.futbol.proyectoacd.model.apifootball.LineupData;
import com.futbol.proyectoacd.model.apifootball.PlayerData;
import com.futbol.proyectoacd.model.apifootball.TeamData;
import com.futbol.proyectoacd.repository.EquipoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de integración para gestionar equipos con datos de API-Football
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EquipoIntegrationService {

    private final ApiFootballService apiFootballService;
    private final ApiFootballMapperService mapperService;
    private final EquipoRepository equipoRepository;

    /**
     * Crea un equipo en nuestra BD a partir de un equipo de API-Football
     * @param apiTeamId ID del equipo en API-Football
     * @param user Usuario que crea el equipo
     * @param season Temporada para obtener jugadores
     * @return Equipo creado
     */
    @Transactional
    public Equipo createEquipoFromApiFootball(Integer apiTeamId, User user, Integer season) {
        log.info("Creando equipo desde API-Football con ID: {}", apiTeamId);

        // 1. Obtener datos del equipo
        TeamData teamData = apiFootballService.getTeamById(apiTeamId);
        if (teamData == null || teamData.getTeam() == null) {
            throw new RuntimeException("No se pudo obtener información del equipo");
        }

        // 2. Obtener jugadores del equipo
        List<PlayerData> players = apiFootballService.getTeamPlayers(apiTeamId, season);

        // 3. Crear alineación básica
        EquipoDetalles alineacion = mapperService.createLineupFromPlayers(
                players,
                apiTeamId,
                "4-3-3"  // Formación por defecto
        );

        // 4. Crear entidad Equipo
        Equipo equipo = new Equipo();
        equipo.setNombre(teamData.getTeam().getName());
        equipo.setUser(user);
        equipo.setVotos(0);
        equipo.setAlineacion(alineacion);

        // 5. Guardar en BD
        equipo = equipoRepository.save(equipo);

        log.info("Equipo creado exitosamente: {} con {} jugadores",
                equipo.getNombre(),
                mapperService.getTotalPlayers(alineacion));

        return equipo;
    }

    /**
     * Actualiza la alineación de un equipo con datos de un partido específico
     * @param equipoId ID del equipo en nuestra BD
     * @param fixtureId ID del partido en API-Football
     * @return Equipo actualizado
     */
    @Transactional
    public Equipo updateAlineacionFromFixture(Integer equipoId, Integer fixtureId) {
        log.info("Actualizando alineación del equipo {} con datos del partido {}", equipoId, fixtureId);

        // 1. Buscar equipo en BD
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        // 2. Obtener alineaciones del partido
        List<LineupData> lineups = apiFootballService.getFixtureLineup(fixtureId);

        if (lineups == null || lineups.isEmpty()) {
            throw new RuntimeException("No se encontraron alineaciones para el partido");
        }

        // 3. Buscar la alineación del equipo correcto
        LineupData equipoLineup = null;
        if (equipo.getAlineacion() != null && equipo.getAlineacion().getApiTeamId() != null) {
            Integer apiTeamId = equipo.getAlineacion().getApiTeamId();
            equipoLineup = lineups.stream()
                    .filter(lineup -> lineup.getTeam() != null &&
                            apiTeamId.equals(lineup.getTeam().getId()))
                    .findFirst()
                    .orElse(lineups.get(0));  // Si no coincide, tomar la primera
        } else {
            equipoLineup = lineups.get(0);  // Tomar la primera si no hay ID de API
        }

        // 4. Convertir y actualizar alineación
        EquipoDetalles nuevaAlineacion = mapperService.convertLineupToEquipoDetalles(equipoLineup, fixtureId);
        equipo.setAlineacion(nuevaAlineacion);

        // 5. Guardar cambios
        equipo = equipoRepository.save(equipo);

        log.info("Alineación actualizada para el equipo {}", equipo.getNombre());

        return equipo;
    }

    /**
     * Busca equipos en API-Football y devuelve información para que el usuario elija
     * @param teamName Nombre del equipo a buscar
     * @return Lista de equipos encontrados
     */
    public List<TeamData> searchTeamsInApi(String teamName) {
        log.info("Buscando equipos con nombre: {}", teamName);
        return apiFootballService.searchTeams(teamName);
    }

    /**
     * Obtiene la alineación de un partido sin guardarla
     * @param fixtureId ID del partido en API-Football
     * @return Lista de alineaciones convertidas
     */
    public List<EquipoDetalles> getFixtureLineups(Integer fixtureId) {
        log.info("Obteniendo alineaciones del partido {}", fixtureId);
        List<LineupData> lineups = apiFootballService.getFixtureLineup(fixtureId);
        return mapperService.convertMultipleLineups(lineups, fixtureId);
    }

    /**
     * Crea un equipo personalizado permitiendo al usuario elegir jugadores
     * @param apiTeamId ID del equipo en API-Football
     * @param user Usuario que crea el equipo
     * @param season Temporada
     * @param formation Formación deseada
     * @param titularesIds IDs de los jugadores titulares
     * @param suplentesIds IDs de los jugadores suplentes
     * @return Equipo creado
     */
    @Transactional
    public Equipo createCustomEquipo(
            Integer apiTeamId,
            User user,
            Integer season,
            String formation,
            List<Integer> titularesIds,
            List<Integer> suplentesIds
    ) {
        log.info("Creando equipo personalizado desde API-Football con ID: {}", apiTeamId);

        // 1. Obtener datos del equipo
        TeamData teamData = apiFootballService.getTeamById(apiTeamId);

        // 2. Obtener todos los jugadores
        List<PlayerData> allPlayers = apiFootballService.getTeamPlayers(apiTeamId, season);

        // 3. Filtrar jugadores seleccionados
        List<EquipoDetalles.JugadorPosicion> titulares = allPlayers.stream()
                .filter(p -> titularesIds.contains(p.getPlayer().getId()))
                .map(p -> convertPlayerToJugadorPosicion(p))
                .toList();

        List<EquipoDetalles.JugadorPosicion> suplentes = allPlayers.stream()
                .filter(p -> suplentesIds.contains(p.getPlayer().getId()))
                .map(p -> convertPlayerToJugadorPosicion(p))
                .toList();

        // 4. Crear alineación personalizada
        EquipoDetalles alineacion = new EquipoDetalles();
        alineacion.setFormacion(formation);
        alineacion.setApiTeamId(apiTeamId);
        alineacion.setLogoEquipo(teamData.getTeam().getLogo());
        alineacion.setTitulares(titulares);
        alineacion.setSuplentes(suplentes);

        // 5. Crear y guardar equipo
        Equipo equipo = new Equipo();
        equipo.setNombre(teamData.getTeam().getName());
        equipo.setUser(user);
        equipo.setVotos(0);
        equipo.setAlineacion(alineacion);

        equipo = equipoRepository.save(equipo);

        log.info("Equipo personalizado creado: {}", equipo.getNombre());

        return equipo;
    }

    /**
     * Convierte PlayerData a JugadorPosicion
     */
    private EquipoDetalles.JugadorPosicion convertPlayerToJugadorPosicion(PlayerData playerData) {
        String position = null;
        Integer number = null;

        if (playerData.getStatistics() != null && !playerData.getStatistics().isEmpty()) {
            PlayerData.Statistics stats = playerData.getStatistics().get(0);
            position = stats.getPosition();
            if (stats.getGames() != null) {
                number = stats.getGames().getNumber();
            }
        }

        return new EquipoDetalles.JugadorPosicion(
                playerData.getPlayer().getId(),
                playerData.getPlayer().getName(),
                number,
                position,
                null
        );
    }

    /**
     * Obtiene los jugadores disponibles de un equipo de API-Football
     * @param apiTeamId ID del equipo en API-Football
     * @param season Temporada
     * @return Lista de jugadores
     */
    public List<PlayerData> getAvailablePlayers(Integer apiTeamId, Integer season) {
        log.info("Obteniendo jugadores disponibles del equipo {} para temporada {}", apiTeamId, season);
        return apiFootballService.getTeamPlayers(apiTeamId, season);
    }
}
