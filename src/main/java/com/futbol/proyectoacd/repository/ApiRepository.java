package com.futbol.proyectoacd.repository;

import com.futbol.proyectoacd.model.Api;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiRepository extends JpaRepository<Api, Integer> {
    Optional<Api> findByIdApi(String idApi);
    boolean existsByIdApi(String idApi);
}
