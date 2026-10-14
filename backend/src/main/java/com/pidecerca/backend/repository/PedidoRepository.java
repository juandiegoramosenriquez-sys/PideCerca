package com.pidecerca.backend.repository;

import com.pidecerca.backend.entity.EstadoPedido;
import com.pidecerca.backend.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findAllByOrderByFechaDesc();

    List<Pedido> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    List<Pedido> findByNegocioIdOrderByFechaDesc(Long negocioId);

    List<Pedido> findByNegocioIdAndEstadoInOrderByFechaAsc(Long negocioId, Collection<EstadoPedido> estados);

    List<Pedido> findByEstadoOrderByFechaDesc(EstadoPedido estado);

    long countByEstado(EstadoPedido estado);

    long countByFechaAfter(LocalDateTime fecha);

    boolean existsByNegocioId(Long negocioId);

    boolean existsByUsuarioId(Long usuarioId);

    @Query("select coalesce(sum(p.total), 0) from Pedido p where p.estado <> com.pidecerca.backend.entity.EstadoPedido.CANCELADO and p.fecha >= :desde")
    Double sumarVentasDesde(@Param("desde") LocalDateTime desde);

    @Query("select coalesce(sum(p.total), 0) from Pedido p where p.estado <> com.pidecerca.backend.entity.EstadoPedido.CANCELADO")
    Double sumarVentasTotales();
}
