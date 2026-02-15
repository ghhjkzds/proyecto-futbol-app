package com.futbol.proyectoacd.model.apifootball;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Modelo para el equipo de la API-Football
 */
@Data
public class TeamData {
    @JsonProperty("team")
    private Team team;

    @JsonProperty("venue")
    private Venue venue;

    @Data
    public static class Team {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("code")
        private String code;

        @JsonProperty("country")
        private String country;

        @JsonProperty("founded")
        private Integer founded;

        @JsonProperty("national")
        private Boolean national;

        @JsonProperty("logo")
        private String logo;
    }

    @Data
    public static class Venue {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("address")
        private String address;

        @JsonProperty("city")
        private String city;

        @JsonProperty("capacity")
        private Integer capacity;

        @JsonProperty("surface")
        private String surface;

        @JsonProperty("image")
        private String image;
    }
}
