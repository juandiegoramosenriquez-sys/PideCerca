package com.pidecerca.backend.repository;

import com.pidecerca.backend.entity.Negocio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NegocioRepository extends JpaRepository<Negocio, Long> {

    Optional<Negocio> findFirstByPropietarioId(Long usuarioId);

    List<Negocio> findByActivoTrue();

    List<Negocio> findAllByOrderByIdDesc();

    long countByActivoTrue();
}
