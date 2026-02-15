package com.futbol.proyectoacd.dto;

import lombok.Data;

@Data
public class CrearComentarioRequest {
    private Integer alineacionId;
    private String mensaje;
    private Integer respondeAId; // null si es comentario raíz
}
