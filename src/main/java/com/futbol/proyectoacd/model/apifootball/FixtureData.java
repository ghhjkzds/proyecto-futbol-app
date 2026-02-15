package com.futbol.proyectoacd.model.apifootball;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Modelo para los partidos de la API-Football
 * Endpoint: /fixtures
 */
@Data
public class FixtureData {
    @JsonProperty("fixture")
    private Fixture fixture;

    @JsonProperty("league")
    private League league;

    @JsonProperty("teams")
    private Teams teams;

    @JsonProperty("goals")
    private Goals goals;

    @JsonProperty("score")
    private Score score;

    @Data
    public static class Fixture {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("referee")
        private String referee;

        @JsonProperty("timezone")
        private String timezone;

        @JsonProperty("date")
        private String date;

        @JsonProperty("timestamp")
        private Long timestamp;

        @JsonProperty("periods")
        private Periods periods;

        @JsonProperty("venue")
        private Venue venue;

        @JsonProperty("status")
        private Status status;
    }

    @Data
    public static class Periods {
        @JsonProperty("first")
        private Long first;

        @JsonProperty("second")
        private Long second;
    }

    @Data
    public static class Venue {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("city")
        private String city;
    }

    @Data
    public static class Status {
        @JsonProperty("long")
        private String longStatus;

        @JsonProperty("short")
        private String shortStatus;

        @JsonProperty("elapsed")
        private Integer elapsed;
    }

    @Data
    public static class League {
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

        @JsonProperty("round")
        private String round;
    }

    @Data
    public static class Teams {
        @JsonProperty("home")
        private TeamInfo home;

        @JsonProperty("away")
        private TeamInfo away;
    }

    @Data
    public static class TeamInfo {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("logo")
        private String logo;

        @JsonProperty("winner")
        private Boolean winner;
    }

    @Data
    public static class Goals {
        @JsonProperty("home")
        private Integer home;

        @JsonProperty("away")
        private Integer away;
    }

    @Data
    public static class Score {
        @JsonProperty("halftime")
        private ScoreDetail halftime;

        @JsonProperty("fulltime")
        private ScoreDetail fulltime;

        @JsonProperty("extratime")
        private ScoreDetail extratime;

        @JsonProperty("penalty")
        private ScoreDetail penalty;
    }

    @Data
    public static class ScoreDetail {
        @JsonProperty("home")
        private Integer home;

        @JsonProperty("away")
        private Integer away;
    }
}
