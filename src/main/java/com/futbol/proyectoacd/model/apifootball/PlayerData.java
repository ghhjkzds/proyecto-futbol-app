package com.futbol.proyectoacd.model.apifootball;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Modelo para los jugadores de un equipo de la API-Football
 */
@Data
public class PlayerData {
    @JsonProperty("player")
    private Player player;

    @JsonProperty("statistics")
    private List<Statistics> statistics;

    @Data
    public static class Player {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("firstname")
        private String firstname;

        @JsonProperty("lastname")
        private String lastname;

        @JsonProperty("age")
        private Integer age;

        @JsonProperty("birth")
        private Birth birth;

        @JsonProperty("nationality")
        private String nationality;

        @JsonProperty("height")
        private String height;

        @JsonProperty("weight")
        private String weight;

        @JsonProperty("injured")
        private Boolean injured;

        @JsonProperty("photo")
        private String photo;
    }

    @Data
    public static class Birth {
        @JsonProperty("date")
        private String date;

        @JsonProperty("place")
        private String place;

        @JsonProperty("country")
        private String country;
    }

    @Data
    public static class Statistics {
        @JsonProperty("team")
        private TeamInfo team;

        @JsonProperty("league")
        private LeagueInfo league;

        @JsonProperty("games")
        private Games games;

        @JsonProperty("position")
        private String position;

        @JsonProperty("rating")
        private String rating;
    }

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
    public static class LeagueInfo {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("country")
        private String country;

        @JsonProperty("logo")
        private String logo;

        @JsonProperty("flag")
        private String flag;

        @JsonProperty("season")
        private Integer season;
    }

    @Data
    public static class Games {
        @JsonProperty("appearences")
        private Integer appearences;

        @JsonProperty("lineups")
        private Integer lineups;

        @JsonProperty("minutes")
        private Integer minutes;

        @JsonProperty("number")
        private Integer number;

        @JsonProperty("position")
        private String position;

        @JsonProperty("rating")
        private String rating;

        @JsonProperty("captain")
        private Boolean captain;
    }
}
