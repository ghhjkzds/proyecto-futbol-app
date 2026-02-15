package com.futbol.proyectoacd.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Detalles de la alineación de un equipo
 * Compatible con datos de API-Football
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EquipoDetalles {
    private String formacion;
    private String entrenador;
    private List<JugadorPosicion> titulares;
    private List<JugadorPosicion> suplentes;

    // Datos adicionales de API-Football
    private Integer apiTeamId;  // ID del equipo en API-Football
    private Integer apiFixtureId;  // ID del partido en API-Football
    private String logoEquipo;

    // Constructor para compatibilidad con datos antiguos
    public EquipoDetalles(String formacion, String entrenador, List<String> posicionJugador, List<String> reservas) {
        this.formacion = formacion;
        this.entrenador = entrenador;
        // Convertir listas simples a JugadorPosicion si es necesario
        if (posicionJugador != null) {
            this.titulares = posicionJugador.stream()
                    .map(nombre -> {
                        JugadorPosicion j = new JugadorPosicion();
                        j.setNombre(nombre);
                        return j;
                    })
                    .toList();
        }
        if (reservas != null) {
            this.suplentes = reservas.stream()
                    .map(nombre -> {
                        JugadorPosicion j = new JugadorPosicion();
                        j.setNombre(nombre);
                        return j;
                    })
                    .toList();
        }
    }

    /**
     * Información de un jugador en una posición específica
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class JugadorPosicion {
        private Integer id;  // ID del jugador en API-Football
        private String nombre;
        private Integer numero;
        private String posicion;  // GK, DEF, MID, FWD
        private String grid;  // Posición en la grilla (ej: "1:1" para portero)
    }
}

