package com.futbol.proyectoacd.repository;

import com.futbol.proyectoacd.model.Equipo;
import com.futbol.proyectoacd.model.Partido;
import com.futbol.proyectoacd.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PartidoRepository extends JpaRepository<Partido, Integer> {
    List<Partido> findByCreadoPor(User user);
    List<Partido> findByOrderByFechaDesc();
    Optional<Partido> findByEquipoLocalAndEquipoVisitanteAndFecha(Equipo equipoLocal, Equipo equipoVisitante, LocalDateTime fecha);
}
