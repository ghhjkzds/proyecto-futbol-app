package com.futbol.proyectoacd.repository;

import com.futbol.proyectoacd.model.Alineacion;
import com.futbol.proyectoacd.model.Comentario;
import com.futbol.proyectoacd.model.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {
    List<Comentario> findByEquipo(Equipo equipo);
    List<Comentario> findByAlineacion(Alineacion alineacion);
    List<Comentario> findByAlineacionOrderByCreatedAtDesc(Alineacion alineacion);
    List<Comentario> findByRespondeA(Comentario comentario);
}
