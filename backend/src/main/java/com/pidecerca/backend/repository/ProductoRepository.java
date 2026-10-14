package com.pidecerca.backend.repository;

import com.pidecerca.backend.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByNegocioIdOrderByNombreAsc(Long negocioId);

    List<Producto> findByNegocioIdAndDisponibleTrueOrderByNombreAsc(Long negocioId);

    boolean existsByCategoriaId(Long categoriaId);

    long countByNegocioId(Long negocioId);
}
