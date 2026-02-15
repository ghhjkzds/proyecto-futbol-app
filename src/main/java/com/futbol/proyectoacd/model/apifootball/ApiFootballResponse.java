package com.futbol.proyectoacd.model.apifootball;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Modelo para la respuesta de la API de API-Football
 * Documentación: https://www.api-football.com/documentation-v3
 */
@Data
public class ApiFootballResponse<T> {
    @JsonProperty("get")
    private String get;

    @JsonProperty("parameters")
    private Object parameters;

    @JsonProperty("errors")
    private Object errors;  // Puede ser List<String> o un Object con mensajes de error

    @JsonProperty("results")
    private Integer results;

    @JsonProperty("paging")
    private Paging paging;

    @JsonProperty("response")
    private List<T> response;

    @Data
    public static class Paging {
        @JsonProperty("current")
        private Integer current;

        @JsonProperty("total")
        private Integer total;
    }
}
