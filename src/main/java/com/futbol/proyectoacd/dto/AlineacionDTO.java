package com.futbol.proyectoacd.dto;

import com.futbol.proyectoacd.model.EquipoDetalles;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlineacionDTO {
    private Integer id;
    private Integer partidoId;
    private String partidoNombre;
    private LocalDateTime partidoFecha;
    private Integer equipoId;
    private String equipoNombre;
    private Integer votos; // Votos de esta alineación específica
    private EquipoDetalles alineacion;
    private LocalDateTime createdAt;
    private String createdBy;
}
