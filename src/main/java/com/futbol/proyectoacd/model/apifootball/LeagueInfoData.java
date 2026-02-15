package com.futbol.proyectoacd.model.apifootball;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Modelo para la información de ligas desde API-Football
 * Endpoint: /leagues
 */
@Data
public class LeagueInfoData {
    @JsonProperty("league")
    private League league;

    @JsonProperty("country")
    private Country country;

    @JsonProperty("seasons")
    private List<Season> seasons;

    @Data
    public static class League {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("type")
        private String type;

        @JsonProperty("logo")
        private String logo;
    }

    @Data
    public static class Country {
        @JsonProperty("name")
        private String name;

        @JsonProperty("code")
        private String code;

        @JsonProperty("flag")
        private String flag;
    }

    @Data
    public static class Season {
        @JsonProperty("year")
        private Integer year;

        @JsonProperty("start")
        private String start;

        @JsonProperty("end")
        private String end;

        @JsonProperty("current")
        private Boolean current;

        @JsonProperty("coverage")
        private Coverage coverage;
    }

    @Data
    public static class Coverage {
        @JsonProperty("fixtures")
        private Fixtures fixtures;

        @JsonProperty("standings")
        private Boolean standings;

        @JsonProperty("players")
        private Boolean players;

        @JsonProperty("top_scorers")
        private Boolean topScorers;

        @JsonProperty("top_assists")
        private Boolean topAssists;

        @JsonProperty("top_cards")
        private Boolean topCards;

        @JsonProperty("injuries")
        private Boolean injuries;

        @JsonProperty("predictions")
        private Boolean predictions;

        @JsonProperty("odds")
        private Boolean odds;
    }

    @Data
    public static class Fixtures {
        @JsonProperty("events")
        private Boolean events;

        @JsonProperty("lineups")
        private Boolean lineups;

        @JsonProperty("statistics_fixtures")
        private Boolean statisticsFixtures;

        @JsonProperty("statistics_players")
        private Boolean statisticsPlayers;
    }
}
