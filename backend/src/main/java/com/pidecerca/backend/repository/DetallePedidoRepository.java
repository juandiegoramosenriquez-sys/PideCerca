package com.pidecerca.backend.repository;

import com.pidecerca.backend.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    List<DetallePedido> findByPedidoId(Long pedidoId);

    boolean existsByProductoId(Long productoId);
}
