package com.futbol.proyectoacd.service;

import com.futbol.proyectoacd.model.EquipoDetalles;
import com.futbol.proyectoacd.model.apifootball.LineupData;
import com.futbol.proyectoacd.model.apifootball.PlayerData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para convertir datos de API-Football a nuestro modelo interno
 */
@Service
@Slf4j
public class ApiFootballMapperService {

    /**
     * Convierte alineación de API-Football a EquipoDetalles
     * @param lineupData Datos de la alineación desde API-Football
     * @param fixtureId ID del partido (opcional)
     * @return EquipoDetalles con la información de la alineación
     */
    public EquipoDetalles convertLineupToEquipoDetalles(LineupData lineupData, Integer fixtureId) {
        if (lineupData == null) {
            throw new IllegalArgumentException("LineupData no puede ser null");
        }

        EquipoDetalles detalles = new EquipoDetalles();

        // Formación
        detalles.setFormacion(lineupData.getFormation());

        // Entrenador
        if (lineupData.getCoach() != null) {
            detalles.setEntrenador(lineupData.getCoach().getName());
        }

        // Datos de API-Football
        if (lineupData.getTeam() != null) {
            detalles.setApiTeamId(lineupData.getTeam().getId());
            detalles.setLogoEquipo(lineupData.getTeam().getLogo());
        }
        detalles.setApiFixtureId(fixtureId);

        // Jugadores titulares
        if (lineupData.getStartXI() != null) {
            List<EquipoDetalles.JugadorPosicion> titulares = lineupData.getStartXI().stream()
                    .map(startPlayer -> {
                        LineupData.PlayerInfo player = startPlayer.getPlayer();
                        return new EquipoDetalles.JugadorPosicion(
                                player.getId(),
                                player.getName(),
                                player.getNumber(),
                                player.getPos(),
                                player.getGrid()
                        );
                    })
                    .collect(Collectors.toList());
            detalles.setTitulares(titulares);
        }

        // Jugadores suplentes
        if (lineupData.getSubstitutes() != null) {
            List<EquipoDetalles.JugadorPosicion> suplentes = lineupData.getSubstitutes().stream()
                    .map(substitute -> {
                        LineupData.PlayerInfo player = substitute.getPlayer();
                        return new EquipoDetalles.JugadorPosicion(
                                player.getId(),
                                player.getName(),
                                player.getNumber(),
                                player.getPos(),
                                player.getGrid()
                        );
                    })
                    .collect(Collectors.toList());
            detalles.setSuplentes(suplentes);
        }

        log.info("Convertida alineación del equipo {} con formación {}",
                lineupData.getTeam() != null ? lineupData.getTeam().getName() : "N/A",
                detalles.getFormacion());

        return detalles;
    }

    /**
     * Crea una alineación básica a partir de la lista de jugadores del equipo
     * Útil cuando no hay un partido específico
     * @param players Lista de jugadores del equipo
     * @param teamId ID del equipo en API-Football
     * @param formation Formación deseada (ej: "4-3-3")
     * @return EquipoDetalles con jugadores organizados por posición
     */
    public EquipoDetalles createLineupFromPlayers(List<PlayerData> players, Integer teamId, String formation) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("La lista de jugadores no puede estar vacía");
        }

        EquipoDetalles detalles = new EquipoDetalles();
        detalles.setFormacion(formation != null ? formation : "4-3-3");
        detalles.setApiTeamId(teamId);

        List<EquipoDetalles.JugadorPosicion> titulares = new ArrayList<>();
        List<EquipoDetalles.JugadorPosicion> suplentes = new ArrayList<>();

        // Agrupar jugadores por posición
        for (PlayerData playerData : players) {
            if (playerData.getStatistics() != null && !playerData.getStatistics().isEmpty()) {
                PlayerData.Statistics stats = playerData.getStatistics().get(0);
                String position = stats.getPosition();
                Integer lineups = stats.getGames() != null ? stats.getGames().getLineups() : 0;

                EquipoDetalles.JugadorPosicion jugador = new EquipoDetalles.JugadorPosicion(
                        playerData.getPlayer().getId(),
                        playerData.getPlayer().getName(),
                        stats.getGames() != null ? stats.getGames().getNumber() : null,
                        position,
                        null  // grid no disponible sin partido
                );

                // Los jugadores con más apariciones como titular van a titulares
                if (lineups != null && lineups > 5) {
                    titulares.add(jugador);
                } else {
                    suplentes.add(jugador);
                }
            }
        }

        detalles.setTitulares(titulares);
        detalles.setSuplentes(suplentes);

        log.info("Creada alineación genérica con {} titulares y {} suplentes",
                titulares.size(), suplentes.size());

        return detalles;
    }

    /**
     * Convierte múltiples alineaciones (útil cuando hay dos equipos en un partido)
     * @param lineups Lista de alineaciones
     * @param fixtureId ID del partido
     * @return Lista de EquipoDetalles
     */
    public List<EquipoDetalles> convertMultipleLineups(List<LineupData> lineups, Integer fixtureId) {
        if (lineups == null || lineups.isEmpty()) {
            return List.of();
        }

        return lineups.stream()
                .map(lineup -> convertLineupToEquipoDetalles(lineup, fixtureId))
                .collect(Collectors.toList());
    }

    /**
     * Extrae el nombre del entrenador de una alineación
     * @param lineupData Datos de la alineación
     * @return Nombre del entrenador o "N/A"
     */
    public String extractCoachName(LineupData lineupData) {
        if (lineupData != null && lineupData.getCoach() != null) {
            return lineupData.getCoach().getName();
        }
        return "N/A";
    }

    /**
     * Cuenta el total de jugadores (titulares + suplentes)
     * @param detalles Detalles del equipo
     * @return Total de jugadores
     */
    public int getTotalPlayers(EquipoDetalles detalles) {
        int count = 0;
        if (detalles.getTitulares() != null) {
            count += detalles.getTitulares().size();
        }
        if (detalles.getSuplentes() != null) {
            count += detalles.getSuplentes().size();
        }
        return count;
    }

    /**
     * Obtiene jugadores por posición
     * @param detalles Detalles del equipo
     * @param position Posición a filtrar (GK, DEF, MID, FWD)
     * @return Lista de jugadores de esa posición
     */
    public List<EquipoDetalles.JugadorPosicion> getPlayersByPosition(EquipoDetalles detalles, String position) {
        List<EquipoDetalles.JugadorPosicion> result = new ArrayList<>();

        if (detalles.getTitulares() != null) {
            result.addAll(detalles.getTitulares().stream()
                    .filter(j -> position.equalsIgnoreCase(j.getPosicion()))
                    .collect(Collectors.toList()));
        }

        if (detalles.getSuplentes() != null) {
            result.addAll(detalles.getSuplentes().stream()
                    .filter(j -> position.equalsIgnoreCase(j.getPosicion()))
                    .collect(Collectors.toList()));
        }

        return result;
    }
}
