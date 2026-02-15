package com.futbol.proyectoacd.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartidoDTO {
    private Integer id;
    private Integer equipoLocalId;
    private String equipoLocalNombre;
    private Integer equipoVisitanteId;
    private String equipoVisitanteNombre;
    private LocalDateTime fecha;
    private Integer creadoPorId;
    private String creadoPorEmail;
}
