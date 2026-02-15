package com.futbol.proyectoacd.repository;

import com.futbol.proyectoacd.model.Equipo;
import com.futbol.proyectoacd.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Integer> {
    List<Equipo> findByUser(User user);
    List<Equipo> findByOrderByVotosDesc();
    Optional<Equipo> findByNombre(String nombre);
}
