package com.futbol.proyectoacd.service;

import com.futbol.proyectoacd.model.apifootball.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;

/**
 * Servicio para consumir la API de API-Football
 * Documentación: https://www.api-football.com/documentation-v3
 */
@Service
@Slf4j
public class ApiFootballService {

    private final WebClient webClient;
    private final String apiKey;

    public ApiFootballService(
            WebClient.Builder webClientBuilder,
            @Value("${api.football.key:YOUR_API_KEY_HERE}") String apiKey,
            @Value("${api.football.base-url:https://v3.football.api-sports.io}") String baseUrl
    ) {
        this.apiKey = apiKey;
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-apisports-key", apiKey)  // Cambiado de x-rapidapi-key a x-apisports-key
                .build();

        log.info("ApiFootballService inicializado con URL: {}", baseUrl);
        log.info("API Key configurada (primeros 10 caracteres): {}...",
                apiKey != null && apiKey.length() > 10 ? apiKey.substring(0, 10) : "NO_CONFIGURADA");
    }

    /**
     * Buscar equipos por nombre
     * @param teamName Nombre del equipo a buscar
     * @return Lista de equipos encontrados
     */
    public List<TeamData> searchTeams(String teamName) {
        try {
            log.info("Buscando equipos con nombre: {}", teamName);

            ApiFootballResponse<TeamData> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/teams")
                            .queryParam("search", teamName)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiFootballResponse<TeamData>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response != null && response.getResponse() != null) {
                log.info("Encontrados {} equipos", response.getResults());
                return response.getResponse();
            }

            return List.of();
        } catch (WebClientResponseException e) {
            log.error("Error al buscar equipos: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error al buscar equipos en API-Football: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al buscar equipos", e);
            throw new RuntimeException("Error al conectar con API-Football: " + e.getMessage());
        }
    }

    /**
     * Obtener información de un equipo por su ID
     * @param teamId ID del equipo en API-Football
     * @return Datos del equipo
     */
    public TeamData getTeamById(Integer teamId) {
        try {
            log.info("Obteniendo información del equipo con ID: {}", teamId);

            ApiFootballResponse<TeamData> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/teams")
                            .queryParam("id", teamId)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiFootballResponse<TeamData>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();

            // Log de errores de la API si existen
            if (response != null && response.getErrors() != null) {
                log.warn("API devolvió errores en /teams (getTeamById): {}", response.getErrors());
            }

            if (response != null && response.getResponse() != null && !response.getResponse().isEmpty()) {
                return response.getResponse().get(0);
            }

            throw new RuntimeException("Equipo no encontrado con ID: " + teamId);
        } catch (WebClientResponseException e) {
            log.error("Error al obtener equipo: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error al obtener equipo de API-Football: " + e.getMessage());
        }
    }

    /**
     * Obtener jugadores de un equipo para una temporada específica
     * Usa el endpoint /players/squads que es más eficiente para obtener la plantilla completa
     * @param teamId ID del equipo
     * @param season Temporada (ej: 2024) - No se usa en /players/squads pero se mantiene por compatibilidad
     * @return Lista de jugadores del equipo convertidos a formato PlayerData
     */
    public List<PlayerData> getTeamPlayers(Integer teamId, Integer season) {
        try {
            log.info("Obteniendo plantilla del equipo {} usando /players/squads", teamId);

            ApiFootballResponse<SquadData> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/players/squads")
                            .queryParam("team", teamId)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiFootballResponse<SquadData>>() {})
                    .timeout(Duration.ofSeconds(15))
                    .block();

            // Log de errores de la API si existen
            if (response != null && response.getErrors() != null) {
                log.warn("API devolvió errores en /players/squads: {}", response.getErrors());
            }

            if (response != null && response.getResponse() != null && !response.getResponse().isEmpty()) {
                SquadData squadData = response.getResponse().get(0);
                log.info("Encontrados {} jugadores en la plantilla del equipo {}",
                         squadData.getPlayers().size(), squadData.getTeam().getName());

                // Convertir SquadPlayer a PlayerData para mantener compatibilidad
                return convertSquadPlayersToPlayerData(squadData);
            }

            log.warn("No se encontraron jugadores para el equipo {}", teamId);
            return List.of();
        } catch (WebClientResponseException e) {
            log.error("Error al obtener plantilla: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error al obtener plantilla de API-Football: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al obtener plantilla", e);
            throw new RuntimeException("Error al conectar con API-Football: " + e.getMessage());
        }
    }

    /**
     * Convierte la respuesta de /players/squads (SquadData) al formato PlayerData
     * para mantener compatibilidad con el resto del código
     */
    private List<PlayerData> convertSquadPlayersToPlayerData(SquadData squadData) {
        return squadData.getPlayers().stream().map(squadPlayer -> {
            PlayerData playerData = new PlayerData();

            // Crear objeto Player
            PlayerData.Player player = new PlayerData.Player();
            player.setId(squadPlayer.getId());
            player.setName(squadPlayer.getName());
            player.setAge(squadPlayer.getAge());
            player.setPhoto(squadPlayer.getPhoto());
            playerData.setPlayer(player);

            // Crear objeto Statistics básico con la posición
            PlayerData.Statistics stats = new PlayerData.Statistics();
            stats.setPosition(squadPlayer.getPosition());

            // Crear objeto Games con el número
            PlayerData.Games games = new PlayerData.Games();
            games.setNumber(squadPlayer.getNumber());
            games.setPosition(squadPlayer.getPosition());
            stats.setGames(games);

            // Crear objeto TeamInfo
            PlayerData.TeamInfo teamInfo = new PlayerData.TeamInfo();
            teamInfo.setId(squadData.getTeam().getId());
            teamInfo.setName(squadData.getTeam().getName());
            teamInfo.setLogo(squadData.getTeam().getLogo());
            stats.setTeam(teamInfo);

            playerData.setStatistics(List.of(stats));

            return playerData;
        }).toList();
    }

    /**
     * Obtener alineación de un partido específico
     * @param fixtureId ID del partido en API-Football
     * @return Lista de alineaciones (una por equipo)
     */
    public List<LineupData> getFixtureLineup(Integer fixtureId) {
        try {
            log.info("Obteniendo alineación del partido con ID: {}", fixtureId);

            ApiFootballResponse<LineupData> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/fixtures/lineups")
                            .queryParam("fixture", fixtureId)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiFootballResponse<LineupData>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response != null && response.getResponse() != null) {
                log.info("Encontradas {} alineaciones", response.getResults());
                return response.getResponse();
            }

            return List.of();
        } catch (WebClientResponseException e) {
            log.error("Error al obtener alineaciones: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error al obtener alineaciones de API-Football: " + e.getMessage());
        }
    }

    /**
     * Obtener partidos de un equipo para una temporada
     * @param teamId ID del equipo
     * @param season Temporada
     * @return Lista de partidos
     */
    public List<FixtureData> getTeamFixtures(Integer teamId, Integer season) {
        try {
            log.info("Obteniendo partidos del equipo {} para la temporada {}", teamId, season);

            ApiFootballResponse<FixtureData> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/fixtures")
                            .queryParam("team", teamId)
                            .queryParam("season", season)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiFootballResponse<FixtureData>>() {})
                    .timeout(Duration.ofSeconds(15))
                    .block();

            if (response != null && response.getResponse() != null) {
                log.info("Encontrados {} partidos", response.getResults());
                return response.getResponse();
            }

            return List.of();
        } catch (WebClientResponseException e) {
            log.error("Error al obtener partidos: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error al obtener partidos de API-Football: " + e.getMessage());
        }
    }

    /**
     * Obtener partidos próximos de una liga
     * @param leagueId ID de la liga
     * @param season Temporada
     * @param next Cantidad de próximos partidos
     * @return Lista de partidos
     */
    public List<FixtureData> getUpcomingFixtures(Integer leagueId, Integer season, Integer next) {
        try {
            log.info("Obteniendo próximos {} partidos de la liga {}", next, leagueId);

            ApiFootballResponse<FixtureData> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/fixtures")
                            .queryParam("league", leagueId)
                            .queryParam("season", season)
                            .queryParam("next", next)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiFootballResponse<FixtureData>>() {})
                    .timeout(Duration.ofSeconds(15))
                    .block();

            if (response != null && response.getResponse() != null) {
                log.info("Encontrados {} partidos próximos", response.getResults());
                return response.getResponse();
            }

            return List.of();
        } catch (WebClientResponseException e) {
            log.error("Error al obtener partidos próximos: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error al obtener partidos de API-Football: " + e.getMessage());
        }
    }

    /**
     * Obtener la temporada actual de una liga
     * @param leagueId ID de la liga (La Liga = 140)
     * @return Año de la temporada actual
     */
    public Integer getCurrentSeason(Integer leagueId) {
        try {
            log.info("Obteniendo temporada actual de la liga {}", leagueId);

            ApiFootballResponse<LeagueInfoData> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/leagues")
                            .queryParam("id", leagueId)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiFootballResponse<LeagueInfoData>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();

            // Log de errores de la API si existen
            if (response != null && response.getErrors() != null) {
                log.warn("API devolvió errores en /leagues: {}", response.getErrors());
            }

            if (response != null && response.getResponse() != null && !response.getResponse().isEmpty()) {
                LeagueInfoData leagueInfo = response.getResponse().get(0);

                // Buscar la temporada con current = true
                if (leagueInfo.getSeasons() != null) {
                    for (LeagueInfoData.Season season : leagueInfo.getSeasons()) {
                        if (Boolean.TRUE.equals(season.getCurrent())) {
                            log.info("Temporada actual encontrada: {}", season.getYear());
                            return season.getYear();
                        }
                    }
                }

                // Si no hay temporada marcada como current, tomar la última
                if (leagueInfo.getSeasons() != null && !leagueInfo.getSeasons().isEmpty()) {
                    LeagueInfoData.Season lastSeason = leagueInfo.getSeasons().get(leagueInfo.getSeasons().size() - 1);
                    log.warn("No se encontró temporada marcada como 'current', usando la última: {}", lastSeason.getYear());
                    return lastSeason.getYear();
                }
            }

            log.error("No se pudo obtener la temporada de la API, usando año actual como fallback");
            return java.time.Year.now().getValue();

        } catch (WebClientResponseException e) {
            log.error("Error al obtener temporada actual: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            log.warn("Usando año actual como fallback");
            return java.time.Year.now().getValue();
        } catch (Exception e) {
            log.error("Error inesperado al obtener temporada actual", e);
            log.warn("Usando año actual como fallback");
            return java.time.Year.now().getValue();
        }
    }

    /**
     * Obtener partidos programados (scheduled) de una liga desde la base de datos
     * NOTA: Este método ya no consulta la API, los partidos deben ser creados manualmente
     * @param leagueId ID de la liga (La Liga = 140) - DEPRECADO, no se usa
     * @return Lista vacía - usar PartidoRepository directamente
     * @deprecated Usar PartidoRepository.findByOrderByFechaDesc() para obtener partidos de la BD
     */
    @Deprecated
    public List<FixtureData> getScheduledFixtures(Integer leagueId) {
        log.warn("getScheduledFixtures está deprecado - los partidos ahora se obtienen de la base de datos");
        return List.of();
    }


    /**
     * Buscar equipos por liga y temporada
     * @param leagueId ID de la liga (La Liga = 140, Premier = 39, etc.)
     * @param season Temporada
     * @return Lista de equipos de la liga
     */
    public List<TeamData> searchTeamsByLeague(Integer leagueId, Integer season) {
        try {
            log.info("Buscando equipos de la liga {} temporada {}", leagueId, season);

            ApiFootballResponse<TeamData> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/teams")
                            .queryParam("league", leagueId)
                            .queryParam("season", season)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiFootballResponse<TeamData>>() {})
                    .timeout(Duration.ofSeconds(15))
                    .block();

            // Log de errores de la API si existen
            if (response != null && response.getErrors() != null) {
                log.warn("API devolvió errores en /teams (searchTeamsByLeague): {}", response.getErrors());
            }

            if (response != null && response.getResponse() != null) {
                log.info("Encontrados {} equipos de la liga", response.getResults());
                return response.getResponse();
            }

            return List.of();
        } catch (WebClientResponseException e) {
            log.error("Error al buscar equipos de la liga: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error al buscar equipos de la liga en API-Football: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al buscar equipos de la liga", e);
            throw new RuntimeException("Error al conectar con API-Football: " + e.getMessage());
        }
    }

    /**
     * Verificar si la API Key es válida
     * @return true si la key es válida
     */
    public boolean validateApiKey() {
        try {
            webClient.get()
                    .uri("/status")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return true;
        } catch (Exception e) {
            log.error("API Key inválida o error de conexión", e);
            return false;
        }
    }
}
