package com.futbol.proyectoacd.model.apifootball;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Modelo para la respuesta del endpoint /players/squads de API-Football
 */
@Data
public class SquadData {

    @JsonProperty("team")
    private TeamInfo team;

    @JsonProperty("players")
    private List<SquadPlayer> players;

    @Data
    public static class TeamInfo {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("logo")
        private String logo;
    }

    @Data
    public static class SquadPlayer {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("age")
        private Integer age;

        @JsonProperty("number")
        private Integer number;

        @JsonProperty("position")
        private String position;

        @JsonProperty("photo")
        private String photo;
    }
}

