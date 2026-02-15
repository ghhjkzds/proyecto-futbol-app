package com.futbol.proyectoacd.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRankingDTO {
    private Integer userId;
    private String email;
    private Long totalVotos;
    private Long totalAlineaciones;
}

