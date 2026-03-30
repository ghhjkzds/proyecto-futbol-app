package com.futbol.proyectoacd.dto;

import com.futbol.proyectoacd.model.EquipoDetalles;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipoDTO {
    private Integer id;
    private Integer userId;
    private String userEmail;
    private String nombre;
    private EquipoDetalles alineacion;
    private LocalDateTime createdAt;
}
