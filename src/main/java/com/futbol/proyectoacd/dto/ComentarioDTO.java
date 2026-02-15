package com.futbol.proyectoacd.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComentarioDTO {
    private Integer id;
    private Integer equipoId;
    private String equipoNombre;
    private Integer alineacionId;
    private Integer userId;
    private String userEmail;
    private String mensaje;
    private Integer respondeAId;
    private LocalDateTime createdAt;
}
