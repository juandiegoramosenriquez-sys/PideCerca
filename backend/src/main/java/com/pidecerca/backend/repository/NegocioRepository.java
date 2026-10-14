package com.pidecerca.backend.repository;

import com.pidecerca.backend.entity.Negocio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NegocioRepository extends JpaRepository<Negocio, Long> {
}