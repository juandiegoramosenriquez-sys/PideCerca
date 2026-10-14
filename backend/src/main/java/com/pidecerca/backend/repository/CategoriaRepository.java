package com.pidecerca.backend.repository;

import com.pidecerca.backend.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByNegocioIsNullOrderByNombreAsc();

    @Query("select c from Categoria c where c.negocio is null or c.negocio.id = :negocioId order by c.nombre asc")
    List<Categoria> findDisponiblesParaNegocio(@Param("negocioId") Long negocioId);
}
