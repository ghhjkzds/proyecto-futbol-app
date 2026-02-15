package com.futbol.proyectoacd.repository;

import com.futbol.proyectoacd.model.Alineacion;
import com.futbol.proyectoacd.model.Partido;
import com.futbol.proyectoacd.model.Equipo;
import com.futbol.proyectoacd.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlineacionRepository extends JpaRepository<Alineacion, Integer> {
    List<Alineacion> findByPartido(Partido partido);
    Optional<Alineacion> findByPartidoAndEquipo(Partido partido, Equipo equipo);
    List<Alineacion> findByCreatedByOrderByCreatedAtDesc(User user);
    List<Alineacion> findByCreatedBy(User user);

    // Buscar alineación específica por usuario, partido y equipo
    Optional<Alineacion> findByCreatedByAndPartidoAndEquipo(User user, Partido partido, Equipo equipo);

    // Verificar si existe una alineación para usuario, partido y equipo
    boolean existsByCreatedByAndPartidoAndEquipo(User user, Partido partido, Equipo equipo);

    // Obtener ranking de usuarios por total de votos recibidos
    @Query("SELECT a.createdBy.id as userId, a.createdBy.email as email, " +
           "SUM(a.votos) as totalVotos, COUNT(a) as totalAlineaciones " +
           "FROM Alineacion a " +
           "GROUP BY a.createdBy.id, a.createdBy.email " +
           "ORDER BY SUM(a.votos) DESC")
    List<Object[]> findRankingUsuarios();
}
