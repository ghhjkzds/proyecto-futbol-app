package com.futbol.proyectoacd.model.apifootball;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Modelo para las alineaciones de un partido de la API-Football
 * Endpoint: /fixtures/lineups
 */
@Data
public class LineupData {
    @JsonProperty("team")
    private Team team;

    @JsonProperty("formation")
    private String formation;

    @JsonProperty("startXI")
    private List<StartPlayer> startXI;

    @JsonProperty("substitutes")
    private List<Substitute> substitutes;

    @JsonProperty("coach")
    private Coach coach;

    @Data
    public static class Team {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("logo")
        private String logo;

        @JsonProperty("colors")
        private Colors colors;
    }

    @Data
    public static class Colors {
        @JsonProperty("player")
        private ColorInfo player;

        @JsonProperty("goalkeeper")
        private ColorInfo goalkeeper;
    }

    @Data
    public static class ColorInfo {
        @JsonProperty("primary")
        private String primary;

        @JsonProperty("number")
        private String number;

        @JsonProperty("border")
        private String border;
    }

    @Data
    public static class StartPlayer {
        @JsonProperty("player")
        private PlayerInfo player;
    }

    @Data
    public static class Substitute {
        @JsonProperty("player")
        private PlayerInfo player;
    }

    @Data
    public static class PlayerInfo {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("number")
        private Integer number;

        @JsonProperty("pos")
        private String pos;

        @JsonProperty("grid")
        private String grid;
    }

    @Data
    public static class Coach {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("photo")
        private String photo;
    }
}
